/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.testing

import org.gradle.integtests.fixtures.*
import org.junit.Rule
import spock.lang.Issue
import spock.lang.Unroll

import static org.hamcrest.Matchers.*

class TestReportIntegrationTest extends AbstractIntegrationSpec {
    @Rule Sample sample = new Sample(temporaryFolder)

    def "report includes results of most recent invocation"() {
        given:
        buildFile << """
$junitSetup
test { systemProperty 'LogLessStuff', System.getProperty('LogLessStuff') }
"""

        and:
        file("src/test/java/LoggingTest.java") << """
public class LoggingTest {
    @org.junit.Test
    public void test() {
        if (System.getProperty("LogLessStuff", "false").equals("true")) {
            System.out.print("stdout.");
            System.err.print("stderr.");
        } else {
            System.out.print("This is stdout.");
            System.err.print("This is stderr.");
        }
    }
}
"""

        when:
        run "test"

        then:
        def result = new HtmlTestExecutionResult(testDirectory)
        result.testClass("LoggingTest").assertStdout(equalTo("This is stdout."))
        result.testClass("LoggingTest").assertStderr(equalTo("This is stderr."))

        when:
        executer.withArguments("-DLogLessStuff=true")
        run "test"

        then:
        result.testClass("LoggingTest").assertStdout(equalTo("stdout."))
        result.testClass("LoggingTest").assertStderr(equalTo("stderr."))
    }

    @UsesSample("testing/testReport")
    def "can generate report for subprojects"() {
        given:
        sample sample

        when:
        run "testReport"

        then:
        def htmlReport = new HtmlTestExecutionResult(sample.dir, "build/reports/allTests")
        htmlReport.testClass("org.gradle.sample.CoreTest").assertTestCount(1, 0, 0).assertTestPassed("ok").assertStdout(equalTo("hello from CoreTest.\n"))
        htmlReport.testClass("org.gradle.sample.UtilTest").assertTestCount(1, 0, 0).assertTestPassed("ok").assertStdout(equalTo("hello from UtilTest.\n"))
    }

    @Issue("http://issues.gradle.org//browse/GRADLE-2821")
    def "test report task can handle test tasks that did not run tests"() {
        given:
        buildScript """
            apply plugin: 'java'

             $junitSetup

            task otherTests(type: Test) {
                binResultsDir file("bin")
                testSrcDirs = []
                testClassesDir = file("blah")
            }

            task testReport(type: TestReport) {
                reportOn test, otherTests
                destinationDir reporting.file("tr")
            }
        """

        and:
        testClass("Thing")

        when:
        succeeds "testReport"

        then:
        ":otherTests" in skippedTasks
        ":test" in nonSkippedTasks
        new HtmlTestExecutionResult(testDirectory, "build/reports/tr").assertTestClassesExecuted("Thing")
    }

    @Issue("http://issues.gradle.org//browse/GRADLE-2915")
    def "test report task can handle tests tasks not having been executed"() {
        when:
        buildScript """
            apply plugin: 'java'

             $junitSetup

            task testReport(type: TestReport) {
                testResultDirs = [test.binResultsDir]
                destinationDir reporting.file("tr")
            }
        """

        and:
        testClass("Thing")

        then:
        succeeds "testReport"
        succeeds "testReport"
    }

    def "test report task is skipped when there are no results"() {
        given:
        buildScript """
            apply plugin: 'java'

            task testReport(type: TestReport) {
                reportOn test
                destinationDir reporting.file("tr")
            }
        """

        when:
        succeeds "testReport"

        then:
        ":test" in skippedTasks
        ":testReport" in skippedTasks
    }

    @Unroll
    "#type report files are considered outputs"() {
        given:
        buildScript """
            $junitSetup
        """

        and:
        testClass "SomeTest"

        when:
        run "test"

        then:
        ":test" in nonSkippedTasks
        file(reportsDir).exists()

        when:
        run "test"

        then:
        ":test" in skippedTasks
        file(reportsDir).exists()

        when:
        file(reportsDir).deleteDir()
        run "test"

        then:
        ":test" in nonSkippedTasks
        file(reportsDir).exists()

        where:
        type   | reportsDir
        "xml"  | "build/test-results"
        "html" | "build/reports/tests"
    }

    def "results or reports are linked to in error output"() {
        given:
        buildScript """
            $junitSetup
            test {
                reports.all { it.enabled = true }
            }
        """

        and:
        failingTestClass "SomeTest"

        when:
        fails "test"

        then:
        ":test" in nonSkippedTasks
        errorOutput.contains("See the report at: ")

        when:
        buildFile << "\ntest.reports.html.enabled = false\n"
        fails "test"

        then:
        ":test" in nonSkippedTasks
        errorOutput.contains("See the results at: ")

        when:
        buildFile << "\ntest.reports.junitXml.enabled = false\n"
        fails "test"

        then:
        ":test" in nonSkippedTasks
        errorOutput.contains("There were failing tests")
        !errorOutput.contains("See the")
    }


    def "output per test case flag invalidates outputs"() {
        when:
        buildScript """
            $junitSetup
            test.reports.junitXml.outputPerTestCase = false
        """
        testClass "SomeTest"
        succeeds "test"

        then:
        ":test" in nonSkippedTasks

        when:
        buildFile << "\ntest.reports.junitXml.outputPerTestCase = true\n"
        succeeds "test"

        then:
        ":test" in nonSkippedTasks
    }

    def "outputs over lifecycle"() {
        when:
        buildScript """
            $junitSetup
            test.reports.junitXml.outputPerTestCase = true
        """

        file("src/test/java/OutputLifecycleTest.java") << """
            import org.junit.*;

            public class OutputLifecycleTest {

                public OutputLifecycleTest() {
                    System.out.println("constructor out");
                    System.err.println("constructor err");
                }

                @BeforeClass
                public static void beforeClass() {
                    System.out.println("beforeClass out");
                    System.err.println("beforeClass err");
                }

                @Before
                public void beforeTest() {
                    System.out.println("beforeTest out");
                    System.err.println("beforeTest err");
                }

                @Test public void m1() {
                    System.out.println("m1 out");
                    System.err.println("m1 err");
                }

                @Test public void m2() {
                    System.out.println("m2 out");
                    System.err.println("m2 err");
                }

                @After
                public void afterTest() {
                    System.out.println("afterTest out");
                    System.err.println("afterTest err");
                }

                @AfterClass
                public static void afterClass() {
                    System.out.println("afterClass out");
                    System.err.println("afterClass err");
                }
            }
        """

        succeeds "test"

        then:
        def xmlReport = new JUnitXmlTestExecutionResult(testDirectory)
        def clazz = xmlReport.testClass("OutputLifecycleTest")
        clazz.assertTestCaseStderr("m1", is("beforeTest err\nm1 err\nafterTest err\n"))
        clazz.assertTestCaseStderr("m2", is("beforeTest err\nm2 err\nafterTest err\n"))
        clazz.assertTestCaseStdout("m1", is("beforeTest out\nm1 out\nafterTest out\n"))
        clazz.assertTestCaseStdout("m2", is("beforeTest out\nm2 out\nafterTest out\n"))
        clazz.assertStderr(is("beforeClass err\nconstructor err\nconstructor err\nafterClass err\n"))
        clazz.assertStdout(is("beforeClass out\nconstructor out\nconstructor out\nafterClass out\n"))
    }

    String getJunitSetup() {
        """
        apply plugin: 'java'
        repositories { mavenCentral() }
        dependencies { testCompile 'junit:junit:4.11' }
        """
    }

    void failingTestClass(String name) {
        testClass(name, true)
    }

    void testClass(String name, boolean failing = false) {
        file("src/test/java/${name}.java") << """
            public class $name {
                @org.junit.Test
                public void test() {
                    assert false == ${failing};
                }
            }
        """
    }

}

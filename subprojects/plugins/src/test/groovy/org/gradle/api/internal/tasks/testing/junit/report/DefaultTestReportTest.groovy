/*
 * Copyright 2011 the original author or authors.
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
package org.gradle.api.internal.tasks.testing.junit.report

import org.gradle.api.internal.tasks.testing.BuildableTestResultsProvider
import org.gradle.api.internal.tasks.testing.junit.result.TestResultsProvider
import org.gradle.api.tasks.testing.TestResult
import org.gradle.test.fixtures.file.TestFile
import org.gradle.test.fixtures.file.TestNameTestDirectoryProvider
import org.gradle.util.ConfigureUtil
import org.junit.Rule
import spock.lang.Specification

class DefaultTestReportTest extends Specification {
    @Rule
    public final TestNameTestDirectoryProvider tmpDir = new TestNameTestDirectoryProvider()
    final DefaultTestReport report = new DefaultTestReport()
    final TestFile reportDir = tmpDir.file('report')
    final TestFile indexFile = reportDir.file('index.html')
    final TestResultsProvider testResultProvider = Mock()

    def generatesReportWhenThereAreNoTestResults() {
        given:
        emptyResultSet()

        when:
        report.generateReport(testResultProvider, reportDir)

        then:
        def index = results(indexFile)
        index.assertHasTests(0)
        index.assertHasFailures(0)
        index.assertHasNoDuration()
        index.assertHasNoSuccessRate()
        index.assertHasNoNavLinks()
    }

    TestResultsProvider buildResults(Closure closure) {
        ConfigureUtil.configure(closure, new BuildableTestResultsProvider())
    }

    def generatesReportWhichIncludesContentsOfEachTestResultFile() {
        given:
        def testTestResults = buildResults {
            testClassResult("org.gradle.Test") {
                testcase("test1")
                testcase("test2") {
                    stdout "this is\nstandard output"
                    stderr "this is\nstandard error"
                }
            }
            testClassResult("org.gradle.Test2") {
                testcase("test3")
            }
            testClassResult("org.gradle.sub.Test") {
                testcase("test4")
            }
        }

        when:
        report.generateReport(testTestResults, reportDir)

        then:
        def index = results(indexFile)
        index.assertHasTests(4)
        index.assertHasFailures(0)
        index.assertHasSuccessRate(100)
        index.assertHasDuration("0.400s")
        index.assertHasLinkTo('packages/org.gradle', 'org.gradle')
        index.assertHasLinkTo('packages/org.gradle.sub', 'org.gradle.sub')
        index.assertHasLinkTo('classes/org.gradle.Test', 'org.gradle.Test')
        index.packageDetails("org.gradle").assertSuccessRate(100)
        index.packageDetails("org.gradle.sub").assertSuccessRate(100)

        reportDir.file("css/style.css").assertIsFile()

        def packageFile = results(reportDir.file('packages/org.gradle.html'))
        packageFile.assertHasTests(3)
        packageFile.assertHasFailures(0)
        packageFile.assertHasSuccessRate(100)
        packageFile.assertHasDuration("0.300s")
        packageFile.assertHasLinkTo('../index', 'all')
        packageFile.assertHasLinkTo('../classes/org.gradle.Test', 'Test')
        packageFile.assertHasLinkTo('../classes/org.gradle.Test2', 'Test2')

        def testClassFile = results(reportDir.file('classes/org.gradle.Test.html'))
        testClassFile.assertHasTests(2)
        testClassFile.assertHasFailures(0)
        testClassFile.assertHasSuccessRate(100)
        testClassFile.assertHasDuration("0.200s")
        testClassFile.assertHasTest('test1')
        testClassFile.assertHasTest('test2')
        testClassFile.assertHasLinkTo('../index', 'all')
        testClassFile.assertHasLinkTo('../packages/org.gradle', 'org.gradle')
        testClassFile.assertHasStandardOutput('this is\nstandard output')
        testClassFile.assertHasStandardError('this is\nstandard error')
    }

    def generatesReportWhenThereAreFailures() {
        given:
        def testTestResults = buildResults {
            testClassResult("org.gradle.Test") {
                testcase("test1") {
                    duration = 0
                    failure("something failed", "this is the failure\nat someClass")
                }
                testcase("test2") {
                    duration = 0
                    failure("this is the failure", "SomeType: this is the failure.\nat someClass")
                    stdout "this is\nstandard output"
                    stderr "this is\nstandard error"

                }
            }

            testClassResult("org.gradle.Test2") {
                testcase("test1") {
                    duration = 0
                }
            }
            testClassResult("org.gradle.sub.Test") {
                testcase("test1") {
                    duration = 0
                }
            }
        }
        when:
        report.generateReport(testTestResults, reportDir)

        then:
        def index = results(indexFile)
        index.assertHasTests(4)
        index.assertHasFailures(2)
        index.assertHasSuccessRate(50)
        index.assertHasFailedTest('classes/org.gradle.Test', 'test1')
        index.packageDetails("org.gradle").assertSuccessRate(33)
        index.packageDetails("org.gradle.sub").assertSuccessRate(100)

        def packageFile = results(reportDir.file('packages/org.gradle.html'))
        packageFile.assertHasTests(3)
        packageFile.assertHasFailures(2)
        packageFile.assertHasSuccessRate(33)
        packageFile.assertHasFailedTest('../classes/org.gradle.Test', 'test1')

        def testClassFile = results(reportDir.file('classes/org.gradle.Test.html'))
        testClassFile.assertHasTests(2)
        testClassFile.assertHasFailures(2)
        testClassFile.assertHasSuccessRate(0)
        testClassFile.assertHasTest('test1')
        testClassFile.assertHasFailure('test1', 'something failed\n\nthis is the failure\nat someClass\n')
        testClassFile.assertHasTest('test2')
        testClassFile.assertHasFailure('test2', 'SomeType: this is the failure.\nat someClass')
    }

    def generatesReportWhenThereAreIgnoredTests() {
        given:
        def testTestResults = buildResults {
            testClassResult("org.gradle.Test") {
                testcase("test1") {
                    resultType = TestResult.ResultType.SKIPPED
                }
            }
        }
        when:
        report.generateReport(testTestResults, reportDir)

        then:
        def index = results(indexFile)
        index.assertHasTests(1)
        index.assertHasFailures(0)
        index.assertHasSuccessRate(100)

        def packageFile = results(reportDir.file('packages/org.gradle.html'))
        packageFile.assertHasTests(1)
        packageFile.assertHasFailures(0)
        packageFile.assertHasSuccessRate(100)

        def testClassFile = results(reportDir.file('classes/org.gradle.Test.html'))
        testClassFile.assertHasTests(1)
        testClassFile.assertHasFailures(0)
        testClassFile.assertHasSuccessRate(100)
        testClassFile.assertHasTest('test1')
        testClassFile.assertTestIgnored('test1')
    }

    def reportsOnClassesInDefaultPackage() {
        given:
        def testTestResults = buildResults {
            testClassResult("Test") {
                testcase("test1") {
                    duration = 0
                }
            }
        }
        when:
        report.generateReport(testTestResults, reportDir)

        then:
        def index = results(indexFile)
        index.assertHasLinkTo('packages/default-package', 'default-package')
        index.assertHasLinkTo('classes/Test', 'Test')

        def packageFile = results(reportDir.file('packages/default-package.html'))
        packageFile.assertHasLinkTo('../classes/Test', 'Test')
    }

    def escapesHtmlContentInReport() {
        given:
        def testTestResults = buildResults {
            testClassResult("org.gradle.Test") {
                testcase("test1 < test2") {
                    failure("<a failure>", "<a failure>")

                    stdout "</html> & "
                    stderr "</div> & "
                }
            }
        }
        when:
        report.generateReport(testTestResults, reportDir)

        then:
        def testClassFile = results(reportDir.file('classes/org.gradle.Test.html'))
        testClassFile.assertHasTest('test1 < test2')
        testClassFile.assertHasFailure('test1 < test2', '<a failure>')
        testClassFile.assertHasStandardOutput('</html> & ')
        testClassFile.assertHasStandardError('</div> & ')
    }

    def encodesUnicodeCharactersInReport() {
        given:
        def testTestResults = buildResults {
            testClassResult("org.gradle.Test") {
                testcase('\u0107') {
                    duration = 0
                    stdout "out:\u0256"
                    stderr "err:\u0102"
                }
            }
        }
        when:
        report.generateReport(testTestResults, reportDir)

        then:
        def testClassFile = results(reportDir.file('classes/org.gradle.Test.html'))
        testClassFile.assertHasTest('\u0107')
        testClassFile.assertHasStandardOutput('out:\u0256')
        testClassFile.assertHasStandardError('err:\u0102')
    }

    def results(TestFile file) {
        return new HtmlTestResultsFixture(file)
    }

    def emptyResultSet() {
        _ * testResultProvider.visitClasses(_)
    }
}



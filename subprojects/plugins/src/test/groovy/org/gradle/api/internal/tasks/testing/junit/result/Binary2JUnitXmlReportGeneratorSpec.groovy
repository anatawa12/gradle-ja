/*
 * Copyright 2012 the original author or authors.
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

package org.gradle.api.internal.tasks.testing.junit.result

import org.gradle.api.tasks.testing.TestResult
import org.gradle.test.fixtures.file.TestNameTestDirectoryProvider
import org.junit.Rule
import spock.lang.Specification
import org.gradle.api.GradleException

/**
 * by Szczepan Faber, created at: 11/19/12
 */
class Binary2JUnitXmlReportGeneratorSpec extends Specification {

    @Rule private TestNameTestDirectoryProvider temp = new TestNameTestDirectoryProvider()
    private resultsProvider = Mock(TestResultsProvider)
    private generator = new Binary2JUnitXmlReportGenerator(temp.testDirectory, resultsProvider)

    def setup() {
        generator.saxWriter = Mock(JUnitXmlResultWriter)
    }

    def "writes results"() {
        def fooTest = new TestClassResult(100)
            .add(new TestMethodResult("foo", Mock(TestResult)))

        def barTest = new TestClassResult(100)
            .add(new TestMethodResult("bar", Mock(TestResult)))
            .add(new TestMethodResult("bar2", Mock(TestResult)))

        resultsProvider.getResults() >> ['FooTest': fooTest, 'BarTest': barTest]

        when:
        generator.generate()

        then:
        1 * generator.saxWriter.write('FooTest', fooTest, _)
        1 * generator.saxWriter.write('BarTest', barTest, _)
        0 * generator.saxWriter._
    }

    def "adds context information to the failure if something goes wrong"() {
        def fooTest = new TestClassResult(100)
                .add(new TestMethodResult("foo", Mock(TestResult)))

        resultsProvider.getResults() >> ['FooTest': fooTest]
        generator.saxWriter.write('FooTest', fooTest, _) >> { throw new IOException("Boo!") }

        when:
        generator.generate()

        then:
        def ex = thrown(GradleException)
        ex.message.startsWith('Could not write XML test results for FooTest')
        ex.cause.message == "Boo!"
    }
}

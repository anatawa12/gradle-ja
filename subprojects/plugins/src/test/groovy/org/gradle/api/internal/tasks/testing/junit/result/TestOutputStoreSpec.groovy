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

import org.gradle.api.internal.tasks.testing.DefaultTestOutputEvent
import org.gradle.api.internal.tasks.testing.TestDescriptorInternal
import org.gradle.api.tasks.testing.TestOutputEvent
import org.gradle.test.fixtures.file.WorkspaceTest

import static org.gradle.api.tasks.testing.TestOutputEvent.Destination.StdErr
import static org.gradle.api.tasks.testing.TestOutputEvent.Destination.StdOut

class TestOutputStoreSpec extends WorkspaceTest {

    private output = new TestOutputStore(testDirectory)

    TestDescriptorInternal descriptor(String className, Object testId) {
        Stub(TestDescriptorInternal) {
            getClassName() >> className
            getId() >> testId
        }
    }

    def "flushes all output when output finishes"() {
        when:
        def writer = output.writer()
        writer.onOutput(1, 1, output(StdOut, "[out]"))
        writer.onOutput(2, 1, output(StdErr, "[err]"))
        writer.onOutput(1, 1, output(StdErr, "[err]"))
        writer.onOutput(1, 1, output(StdOut, "[out2]"))
        writer.close()
        def reader = output.reader()

        then:
        collectOutput(reader, 1, StdOut) == "[out][out2]"
        collectOutput(reader, 1, StdErr) == "[err]"
        collectOutput(reader, 2, StdErr) == "[err]"
    }

    def DefaultTestOutputEvent output(TestOutputEvent.Destination destination, String msg) {
        new DefaultTestOutputEvent(destination, msg)
    }

    def "writes nothing for unknown test class"() {
        when:
        def writer = output.writer()
        writer.close()

        then:
        def reader = output.reader()
        collectOutput(reader, 20, StdErr) == ""
    }

    def "can query whether output is available for a test class"() {
        when:
        def writer = output.writer()
        writer.onOutput(1, 1, output(StdOut, "[out]"))
        writer.close()
        def reader = output.reader()

        then:
        reader.hasOutput(1, StdOut)
        !reader.hasOutput(1, StdErr)
        !reader.hasOutput(2, StdErr)

        cleanup:
        reader.close()
    }

    def "can open empty reader"() {
        // neither file
        expect:
        output.reader().close() // no exception
    }

    def "exception if no output file"() {
        when:
        output.indexFile.createNewFile()
        def reader = output.reader()

        then:
        thrown(IllegalStateException)

        cleanup:
        reader?.close()
    }

    def "exception if no index file, but index"() {
        when:
        output.outputsFile.createNewFile()
        def reader = output.reader()

        then:
        thrown(IllegalStateException)

        cleanup:
        reader?.close()
    }

    String collectOutput(TestOutputStore.Reader reader, long classId, TestOutputEvent.Destination destination) {
        def writer = new StringWriter()
        reader.writeAllOutput(classId, destination, writer)
        return writer.toString()
    }
}

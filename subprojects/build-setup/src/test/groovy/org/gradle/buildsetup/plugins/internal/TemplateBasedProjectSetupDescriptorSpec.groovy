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

package org.gradle.buildsetup.plugins.internal

import org.gradle.api.internal.DocumentationRegistry
import org.gradle.api.internal.file.FileResolver
import org.gradle.test.fixtures.encoding.Identifier
import org.gradle.test.fixtures.file.TestFile
import org.gradle.test.fixtures.file.TestNameTestDirectoryProvider
import org.junit.Rule
import spock.lang.Specification

class TemplateBasedProjectSetupDescriptorSpec extends Specification {

    private TestTemplateBasedProjectSetupDescriptor descriptor
    private DocumentationRegistry documentationRegistry
    private FileResolver fileResolver
    private TestFile buildTemplateFile
    private TestFile settingsTemplateFile
    private URL buildFileTemplateURL
    private URL settingsTemplateURL
    @Rule
    final TestNameTestDirectoryProvider temporaryFolder = new TestNameTestDirectoryProvider()

    def setup() {
        buildTemplateFile = temporaryFolder.createFile("build.gradle.template")
        buildFileTemplateURL = buildTemplateFile.toURI().toURL()

        settingsTemplateFile = temporaryFolder.createFile("settings.gradle.template")
        settingsTemplateFile << 'root'
        settingsTemplateURL = settingsTemplateFile.toURI().toURL()

        documentationRegistry = Mock(DocumentationRegistry)

        fileResolver = Mock(FileResolver)
        _ * fileResolver.resolve(_) >> {
            temporaryFolder.file(it[0])
        }

        descriptor = new TestTemplateBasedProjectSetupDescriptor(fileResolver, documentationRegistry)
    }

    def "can generate build and settings file via templates"() {
        when:
        descriptor.generateProject()
        then:
        temporaryFolder.file("build.gradle").exists()
        temporaryFolder.file("settings.gradle").exists()
    }

    def "escapes strings and encodes contents using UTF-8"() {
        setup:
        buildTemplateFile.text = '${somePath.groovyComment}'
        settingsTemplateFile.text = '${someValue.groovyString}'
        when:
        descriptor.generateProject()
        then:
        temporaryFolder.file("build.gradle").getText('utf-8') == /C:\\Programe Files\\gradle/
        temporaryFolder.file("settings.gradle").getText('utf-8') == new Identifier(/a\'b\\c/).withNonAscii().toString()
    }

    class TestTemplateBasedProjectSetupDescriptor extends TemplateBasedProjectSetupDescriptor {

        public TestTemplateBasedProjectSetupDescriptor(FileResolver fileResolver, DocumentationRegistry documentationRegistry) {
            super(fileResolver, documentationRegistry)
        }

        @Override
        URL getBuildFileTemplate() {
            return buildFileTemplateURL
        }

        @Override
        URL getSettingsTemplate() {
            return settingsTemplateURL
        }

        @Override
        protected Map getAdditionalBuildFileTemplateBindings() {
            return [somePath: "C:\\Programe Files\\gradle"]
        }

        protected Map getAdditionalSettingsFileTemplateBindings() {
            return [someValue: new Identifier("a\'b\\c").withNonAscii().toString()]
        }

        String getId() {
            return "test-ID"
        }
    }
}

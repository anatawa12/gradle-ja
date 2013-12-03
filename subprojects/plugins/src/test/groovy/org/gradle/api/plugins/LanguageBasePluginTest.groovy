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
package org.gradle.api.plugins
import org.gradle.api.Project
import org.gradle.language.base.BinaryContainer
import org.gradle.language.base.ProjectSourceSet
import org.gradle.language.base.internal.BinaryInternal
import org.gradle.language.base.internal.BinaryNamingScheme
import org.gradle.language.base.plugins.LanguageBasePlugin
import org.gradle.util.TestUtil
import spock.lang.Specification

class LanguageBasePluginTest extends Specification {
    Project project = TestUtil.createRootProject()

    def setup() {
        project.plugins.apply(LanguageBasePlugin)
    }

    def "adds a 'binaries' container to the project"() {
        expect:
        project.extensions.findByName("binaries") instanceof BinaryContainer
    }

    def "adds a 'sources' container to the project"() {
        expect:
        project.extensions.findByName("sources") instanceof ProjectSourceSet
    }

    def "creates a lifecycle task for each binary"() {
        def binary = Mock(BinaryInternal)
        def namingScheme = Mock(BinaryNamingScheme)

        when:
        project.extensions.findByType(BinaryContainer).add(binary)

        then:
        _ * binary.name >> "binaryName"
        1 * binary.namingScheme >> namingScheme
        1 * namingScheme.lifecycleTaskName >> "lifecycle"
        1 * binary.setLifecycleTask({it == project.tasks.findByName("lifecycle")})
    }
}

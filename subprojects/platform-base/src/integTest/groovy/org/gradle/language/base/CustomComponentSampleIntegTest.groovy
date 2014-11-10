/*
 * Copyright 2014 the original author or authors.
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

package org.gradle.language.base

import org.gradle.integtests.fixtures.AbstractIntegrationSpec
import org.gradle.integtests.fixtures.Sample
import org.gradle.util.TextUtil
import org.junit.Rule

class CustomComponentSampleIntegTest extends AbstractIntegrationSpec {
    @Rule Sample customComponent = new Sample(temporaryFolder, "customComponent")

    def "can create custom component with binaries"() {
        given:
        sample customComponent
        customComponent.dir.file("build.gradle") << """


task checkModel << {
    assert project.componentSpecs.size() == 2
    def coreLib = project.componentSpecs.core
    assert coreLib instanceof SampleLibrary
    assert coreLib.projectPath == project.path
    assert coreLib.displayName == "DefaultSampleLibrary 'core'"
    assert coreLib.binaries.collect{it.name}.sort() == ['coreOsxBinary', 'coreUnixBinary', 'coreWindowsBinary']
}

"""
        expect:
        succeeds "checkModel"
    }

    def "can create all binaries"() {
        given:
        sample customComponent
        when:
        succeeds "assemble"
        then:
        output.contains(TextUtil.toPlatformLineSeparators(""":coreOsxBinaryCreationTask
:coreOsxBinary
:coreUnixBinaryCreationTask
:coreUnixBinary
:coreWindowsBinaryCreationTask
:coreWindowsBinary
:featureOsxBinaryCreationTask
:featureOsxBinary
:featureUnixBinaryCreationTask
:featureUnixBinary
:featureWindowsBinaryCreationTask
:featureWindowsBinary
:assemble

BUILD SUCCESSFUL"""))
        and:
        customComponent.dir.file("build/binaries").assertHasDescendants("coreOsxBinary.svg", "coreUnixBinary.svg", "coreWindowsBinary.svg", "featureOsxBinary.svg",
                                                                        "featureUnixBinary.svg", "featureWindowsBinary.svg")
    }
}

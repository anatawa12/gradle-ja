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

package org.gradle.nativecode.base.internal

import org.gradle.api.Task
import org.gradle.api.file.SourceDirectorySet
import org.gradle.internal.reflect.DirectInstantiator
import org.gradle.nativecode.base.Library
import spock.lang.Specification

class DefaultStaticLibraryBinaryTest extends Specification {
    def flavorContainer = new DefaultFlavorContainer(new DirectInstantiator())
    def library = Stub(Library) {
        getName() >> "main"
        getFlavors() >> flavorContainer
    }
    def toolChain = Stub(ToolChainInternal)

    def "has useful string representation"() {
        when:
        flavorContainer.add new DefaultFlavor("flavorOne")

        then:
        staticLibrary.toString() == "static library 'mainStaticLibrary'"

        when: "library has multiple flavors"
        flavorContainer.add new DefaultFlavor("flavorTwo")

        then:
        staticLibrary.toString() == "static library 'flavorOneMainStaticLibrary'"
    }

    def getStaticLibrary() {
        new DefaultStaticLibraryBinary(library, new DefaultFlavor("flavorOne"), toolChain)
    }

    def "can convert binary to a native dependency"() {
        final binary = staticLibrary
        given:
        def headers = Stub(SourceDirectorySet)
        library.headers >> headers
        def lifecycleTask = Stub(Task)
        binary.lifecycleTask = lifecycleTask
        binary.dependsOn(Stub(Task))

        expect:
        def nativeDependency = binary.resolve()
        nativeDependency.includeRoots == headers

        and:
        nativeDependency.linkFiles.files == [binary.outputFile] as Set
        nativeDependency.linkFiles.buildDependencies.getDependencies(Stub(Task)) == [lifecycleTask] as Set
        nativeDependency.linkFiles.toString() == "static library 'mainStaticLibrary'"

        and:
        nativeDependency.runtimeFiles.files.isEmpty()
        nativeDependency.runtimeFiles.buildDependencies.getDependencies(Stub(Task)).isEmpty()
        nativeDependency.runtimeFiles.toString() == "static library 'mainStaticLibrary'"
    }
}

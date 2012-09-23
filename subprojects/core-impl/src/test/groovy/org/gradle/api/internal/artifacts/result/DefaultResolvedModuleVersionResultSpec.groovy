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

package org.gradle.api.internal.artifacts.result

import org.gradle.api.artifacts.result.ModuleVersionSelectionReason
import spock.lang.Specification

import static org.gradle.api.internal.artifacts.DefaultModuleVersionIdentifier.newId
import static org.gradle.api.internal.artifacts.DefaultModuleVersionSelector.newSelector
import static org.gradle.util.Matchers.strictlyEqual

/**
 * Created: 10/08/2012
 * @author Szczepan Faber
 */
class DefaultResolvedModuleVersionResultSpec extends Specification {

    def "equals"() {
        def module = newModule("group", "module", "version")
        def same = newModule("group", "module", "version")

        def differentGroup = newModule("other", "module", "version")
        def differentModule = newModule("group", "other", "version")
        def differentVersion = newModule("group", "module", "other")
        def differentReason = newModule("group", "module", "version", ModuleVersionSelectionReason.conflictResolution)

        expect:
        module strictlyEqual(same)
        module strictlyEqual(differentReason)

        module != differentGroup
        module != differentModule
        module != differentVersion
    }

    def "equals does not consider dependencies and dependents"() {
        def result = newModule("group", "module", "version")
        def differentDeps = newModule("group", "module", "version")
                .addDependency(newDependency())
        def differentDependents = newModule("group", "module", "version")
                .addDependent(newDependency())

        expect:
        result == differentDeps
        result == differentDependents
    }

    def "equals and hashcode do not recurse forever"() {
        given: "simple dependency A->B"
        def depA = newDependency("a", "A", "1")
        def moduleA = depA.selected

        def depB = newDependency("a", "B", "1")
        def moduleB = depB.selected

        when:
        moduleA.addDependency(depB)
        moduleB.addDependent(depA)

        then:
        moduleA.hashCode()
        !moduleA.equals(moduleB)
    }

    def "mutating dependencies is harmless"() {
        given:
        def module = newModule("a", "c", "1")
        def dependency = newDependency("a", "x", "1")

        when:
        module.addDependency(dependency)

        then:
        module.dependencies == [dependency] as Set

        when:
        module.dependencies << newDependency("a", "y", "1")

        then:
        module.dependencies == [dependency] as Set
    }

    def "excludes unresolved dependencies"() {
        given:
        def module = newModule()
        def dependency = newDependency()
        def unresolved = newUnresolved()

        when:
        module.addDependency(dependency)
        module.addDependency(unresolved)

        then:
        module.dependencies == [dependency] as Set
    }

    def newDependency(String group='a', String module='a', String version='1') {
        new DefaultResolvedDependencyResult(newSelector(group, module, version), newModule(group, module, version), newModule())
    }

    def newUnresolved(String group='x', String module='x', String version='1') {
        new DefaultUnresolvedDependencyResult(newSelector(group, module, version), new RuntimeException("boo!"), newModule())
    }

    def newModule(String group='a', String module='a', String version='1', ModuleVersionSelectionReason selectionReason = ModuleVersionSelectionReason.requested) {
        new DefaultResolvedModuleVersionResult(newId(group, module, version), selectionReason)
    }
}

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
package org.gradle.integtests.resolve.ivy

import org.gradle.integtests.fixtures.AbstractDependencyResolutionTest

class IvyResolveIntegrationTest extends AbstractDependencyResolutionTest {
    def "dependency includes all artifacts and transitive dependencies of referenced configuration"() {
        given:
        ivyRepo.module("org.gradle", "test", "1.45")
                .dependsOn("org.gradle", "other", "preview-1")
                .artifact()
                .artifact(classifier: "classifier")
                .artifact(name: "test-extra")
                .publish()

        ivyRepo.module("org.gradle", "other", "preview-1").publish()

        and:
        buildFile << """
repositories { ivy { url "${ivyRepo.uri}" } }
configurations { compile }
dependencies {
    compile "org.gradle:test:1.45"
}

task check << {
    assert configurations.compile.collect { it.name } == ['test-1.45.jar', 'test-1.45-classifier.jar', 'test-extra-1.45.jar', 'other-preview-1.jar']
    def result = configurations.compile.incoming.resolutionResult

    // Check root component
    def rootId = result.root.id
    assert rootId instanceof ModuleComponentIdentifier
    def rootPublishedAs = result.root.publishedAs
    assert rootPublishedAs instanceof ModuleComponentIdentifier
    assert rootPublishedAs.group == rootId.group
    assert rootPublishedAs.module == rootId.module
    assert rootPublishedAs.version == rootId.version

    // Check external module components
    def externalComponents = result.root.dependencies.selected.findAll { it.id instanceof ModuleComponentIdentifier }
    assert externalComponents.size() == 1
    def selectedExternalComponent = externalComponents[0]
    assert selectedExternalComponent.id.group == 'org.gradle'
    assert selectedExternalComponent.id.module == 'test'
    assert selectedExternalComponent.id.version == '1.45'
    assert selectedExternalComponent.id == selectedExternalComponent.publishedAs
}
"""

        expect:
        succeeds "check"
    }

    def "dependency that references a classifier includes the matching artifact only plus the transitive dependencies of referenced configuration"() {
        given:
        ivyRepo.module("org.gradle", "test", "1.45")
                .dependsOn("org.gradle", "other", "preview-1")
                .artifact(classifier: "classifier")
                .artifact(name: "test-extra")
                .publish()
        ivyRepo.module("org.gradle", "other", "preview-1").publish()

        and:
        buildFile << """
repositories { ivy { url "${ivyRepo.uri}" } }
configurations { compile }
dependencies {
    compile "org.gradle:test:1.45:classifier"
}

task check << {
    assert configurations.compile.collect { it.name } == ['test-1.45-classifier.jar', 'other-preview-1.jar']
}
"""

        expect:
        succeeds "check"
    }

    def "dependency that references an artifact includes the matching artifact only plus the transitive dependencies of referenced configuration"() {
        given:
        ivyRepo.module("org.gradle", "test", "1.45")
                .dependsOn("org.gradle", "other", "preview-1")
                .artifact(classifier: "classifier")
                .artifact(name: "test-extra")
                .publish()
        ivyRepo.module("org.gradle", "other", "preview-1").publish()

        and:
        buildFile << """
repositories { ivy { url "${ivyRepo.uri}" } }
configurations { compile }
dependencies {
    compile ("org.gradle:test:1.45") {
        artifact {
            name = 'test-extra'
            type = 'jar'
        }
    }
}

task check << {
    assert configurations.compile.collect { it.name } == ['test-extra-1.45.jar', 'other-preview-1.jar']
}
"""

        expect:
        succeeds "check"
    }

    def "transitive flag of referenced configuration affects its transitive dependencies only"() {
        given:
        ivyRepo.module("org.gradle", "test", "1.45")
                .dependsOn("org.gradle", "other", "preview-1")
                .nonTransitive('default')
                .publish()
        ivyRepo.module("org.gradle", "other", "preview-1").dependsOn("org.gradle", "other2", "7").publish()
        ivyRepo.module("org.gradle", "other2", "7").publish()

        and:
        buildFile << """
repositories { ivy { url "${ivyRepo.uri}" } }
configurations {
    compile
    runtime.extendsFrom compile
}
dependencies {
    compile "org.gradle:test:1.45"
    runtime "org.gradle:other:preview-1"
}

task check << {
    def spec = { it.name == 'test' } as Spec

    assert configurations.compile.collect { it.name } == ['test-1.45.jar', 'other-preview-1.jar']
    assert configurations.compile.resolvedConfiguration.getFiles(spec).collect { it.name } == ['test-1.45.jar', 'other-preview-1.jar']

    assert configurations.runtime.collect { it.name } == ['test-1.45.jar', 'other-preview-1.jar', 'other2-7.jar']
    assert configurations.compile.resolvedConfiguration.getFiles(spec).collect { it.name } == ['test-1.45.jar', 'other-preview-1.jar']
}
"""

        expect:
        succeeds "check"
    }
}

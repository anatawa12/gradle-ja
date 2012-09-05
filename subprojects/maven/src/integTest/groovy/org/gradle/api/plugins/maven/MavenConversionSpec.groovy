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

package org.gradle.api.plugins.maven

import org.gradle.integtests.fixtures.AbstractIntegrationSpec
import org.gradle.integtests.fixtures.Maven3Availability
import org.gradle.integtests.fixtures.TestResources
import org.junit.Rule
import spock.lang.IgnoreIf

/**
 * by Szczepan Faber, created at: 9/4/12
 */
@IgnoreIf({ !Maven3Availability.AVAILABLE })
class MavenConversionSpec extends AbstractIntegrationSpec {

    @Rule public final TestResources resources = new TestResources()

    def "multiModule"() {
        given:
        file("build.gradle") << "apply plugin: 'maven2Gradle'"

        when:
        run 'maven2Gradle'

        then:
        file("settings.gradle").exists()

        and: //can run gradle build
        run 'clean', 'build'
    }

    def "singleModule"() {
        given:
        file("build.gradle") << "apply plugin: 'maven2Gradle'"

        when:
        run 'maven2Gradle'

        then:
        //TODO SF this build should fail because the TestNG test is failing
        //however the plugin does not generate testNG for single module project atm (bug)
        //def failure = runAndFail('clean', 'build')  //assert if fails for the right reason
        run 'clean', 'build'
    }
}
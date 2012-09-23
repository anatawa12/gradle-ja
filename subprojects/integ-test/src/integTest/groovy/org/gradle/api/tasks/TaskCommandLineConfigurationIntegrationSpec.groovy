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

package org.gradle.api.tasks

import org.gradle.integtests.fixtures.AbstractIntegrationSpec

/**
 * by Szczepan Faber, created at: 9/5/12
 */
class TaskCommandLineConfigurationIntegrationSpec extends AbstractIntegrationSpec {

    def "can configure task from command line"() {
        given:
        file("build.gradle") << """
            task someTask(type: SomeTask)

            import org.gradle.api.internal.tasks.CommandLineOption

            class SomeTask extends DefaultTask {

                boolean first
                String second

                @CommandLineOption(options = "first", description = "configures 'first' field")
                void setFirst(boolean first) {
                    this.first = first
                }

                @CommandLineOption(options = "second", description = "configures 'second' field")
                void setSecond(String second) {
                    this.second = second
                }

                //more stress
                void setSecond(Object second) {
                    this.second = second.toString()
                }

                @TaskAction
                void renderFields() {
                    println "first=" + first + ",second=" + second
                }
            }
"""

        when:
        run 'someTask'

        then:
        output.contains 'first=false,second=null'

        when:
        run 'someTask', '--first', '--second', 'hey'

        then:
        output.contains 'first=true,second=hey'
    }

    //TODO SF more coverage for the unhappy scenarios: missing value, too many values
}

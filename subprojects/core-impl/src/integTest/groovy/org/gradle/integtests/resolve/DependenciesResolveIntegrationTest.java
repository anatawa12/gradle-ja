/*
 * Copyright 2009 the original author or authors.
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
package org.gradle.integtests.resolve;

import org.gradle.integtests.fixtures.Sample;
import org.gradle.integtests.fixtures.executer.GradleContextualExecuter;
import org.gradle.integtests.fixtures.executer.GradleDistribution;
import org.gradle.integtests.fixtures.executer.GradleExecuter;
import org.gradle.integtests.fixtures.executer.UnderDevelopmentGradleDistribution;
import org.gradle.test.fixtures.file.TestNameTestDirectoryProvider;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;

/**
 * @author Hans Dockter
 */
public class DependenciesResolveIntegrationTest {
    @Rule public final TestNameTestDirectoryProvider testDirectoryProvider = new TestNameTestDirectoryProvider();
    private final GradleDistribution dist = new UnderDevelopmentGradleDistribution();
    private final GradleExecuter executer = new GradleContextualExecuter(dist, testDirectoryProvider);
    @Rule public final Sample sample = new Sample("dependencies");

    @Test
    public void testResolve() {
        executer.requireOwnGradleUserHomeDir();

        // the actual testing is done in the build script.
        File projectDir = sample.getDir();
        executer.inDirectory(projectDir).withTasks("test").run();
    }   
}

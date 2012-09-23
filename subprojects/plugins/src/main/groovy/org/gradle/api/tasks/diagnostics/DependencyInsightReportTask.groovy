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

package org.gradle.api.tasks.diagnostics;


import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.result.ResolutionResult
import org.gradle.api.artifacts.result.ResolvedDependencyResult
import org.gradle.api.internal.tasks.CommandLineOption
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.diagnostics.internal.GraphRenderer
import org.gradle.api.tasks.diagnostics.internal.graph.DependencyGraphRenderer
import org.gradle.api.tasks.diagnostics.internal.graph.NodeRenderer
import org.gradle.api.tasks.diagnostics.internal.graph.nodes.RenderableDependency
import org.gradle.api.tasks.diagnostics.internal.insight.DependencyInsightReporter
import org.gradle.logging.StyledTextOutput
import org.gradle.logging.StyledTextOutputFactory

import static org.gradle.logging.StyledTextOutput.Style.Info

/**
 * by Szczepan Faber, created at: 8/17/12
 */
public class DependencyInsightReportTask extends DefaultTask {

    Configuration configuration;
    Closure includes;

    private StyledTextOutput output;
    private GraphRenderer renderer;

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    public void setIncludes(Closure includes) {
        this.includes = includes
    }

    @CommandLineOption(options = "includes", description = "Shows the details of given dependency.")
    public void setIncludes(String dependencyNotation) {
        this.includes = { ResolvedDependencyResult candidate ->
            String candidateName = candidate.requested.group + ":" + candidate.requested.name + ":" + candidate.requested.version
            return candidateName.contains(dependencyNotation)
        }
    }

    @CommandLineOption(options = "configuration", description = "Looks for the depedency in given configuration.")
    public void setConfiguration(String configurationName) {
        this.configuration = project.configurations.getByName(configurationName)
    }

    @TaskAction
    public void report() {
        if (configuration == null) {
            throw new ReportException("Dependency insight report cannot be generated because the input configuration was not specified.")
        }
        if (includes == null) {
            throw new ReportException("Dependency insight report cannot be generated because the dependency to include was not specified.")
        }

        output = getServices().get(StyledTextOutputFactory.class).create(getClass());
        renderer = new GraphRenderer(output);

        ResolutionResult result = configuration.getIncoming().getResolutionResult();
        Set<? extends ResolvedDependencyResult> allDependencies = result.getAllDependencies()

        if (allDependencies.empty) {
            output.println("No resolved dependencies found in $configuration")
            return
        }

        Collection<ResolvedDependencyResult> selectedDependencies = allDependencies.findAll { ResolvedDependencyResult it ->
            //TODO SF this is quite crude for now but I need to get some feedback before implementing more.
            includes(it)
        }

        if (selectedDependencies.empty) {
            output.println("No resolved dependencies matching given input were found in $configuration")
            return
        }

        def sortedDeps = new DependencyInsightReporter().prepare(selectedDependencies)

        def nodeRenderer = new NodeRenderer() {
            void renderNode(StyledTextOutput output, RenderableDependency node, Set<RenderableDependency> children, boolean alreadyRendered) {
                boolean leaf = children.empty
                output.text(leaf? DependencyInsightReportTask.this.configuration.name : node.name);
                if (alreadyRendered && !leaf) {
                    output.withStyle(Info).text(" (*)")
                }
            }
        }
        def dependencyGraphRenderer = new DependencyGraphRenderer(renderer, nodeRenderer)

        int i = 1
        for (RenderableDependency dependency: sortedDeps) {
            renderer.visit(new Action<StyledTextOutput>() {
                public void execute(StyledTextOutput out) {
                    out.withStyle(StyledTextOutput.Style.Identifier).text(dependency.name);
                    if (dependency.description) {
                        out.withStyle(StyledTextOutput.Style.Description).text(" (" + dependency.description + ")")
                    }
                }
            }, true);
            dependencyGraphRenderer.render(dependency)
            boolean last = i++ == sortedDeps.size()
            if (!last) {
                output.println()
            }
        }

        dependencyGraphRenderer.printLegend()
    }
}

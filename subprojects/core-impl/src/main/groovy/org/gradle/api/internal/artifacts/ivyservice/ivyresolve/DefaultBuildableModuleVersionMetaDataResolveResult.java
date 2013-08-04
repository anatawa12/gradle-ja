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
package org.gradle.api.internal.artifacts.ivyservice.ivyresolve;

import org.apache.ivy.core.module.descriptor.*;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.gradle.api.artifacts.ModuleVersionIdentifier;
import org.gradle.api.internal.artifacts.DefaultModuleVersionIdentifier;
import org.gradle.api.internal.artifacts.ivyservice.ModuleVersionResolveException;
import org.gradle.util.CollectionUtils;

import java.util.*;

// TODO - split out a buildable ModuleVersionMetaData implementation
public class DefaultBuildableModuleVersionMetaDataResolveResult implements BuildableModuleVersionMetaDataResolveResult, MutableModuleVersionMetaData {
    private static final List<String> DEFAULT_STATUS_SCHEME = Arrays.asList("integration", "milestone", "release");

    private ModuleDescriptor moduleDescriptor;
    private boolean changing;
    private State state = State.Unknown;
    private ModuleSource moduleSource;
    private List<DependencyMetaData> dependencies;
    private Map<String, DefaultConfigurationMetaData> configurations = new HashMap<String, DefaultConfigurationMetaData>();
    private ModuleVersionResolveException failure;
    private ModuleVersionIdentifier moduleVersionIdentifier;
    private String status;
    private List<String> statusScheme = DEFAULT_STATUS_SCHEME;

    public void reset(State state) {
        this.state = state;
        moduleDescriptor = null;
        changing = false;
        failure = null;
        status = null;
        statusScheme = DEFAULT_STATUS_SCHEME;
    }

    public void resolved(ModuleDescriptor descriptor, boolean changing, ModuleSource moduleSource) {
        ModuleRevisionId moduleRevisionId = descriptor.getModuleRevisionId();
        ModuleVersionIdentifier id = DefaultModuleVersionIdentifier.newId(moduleRevisionId);
        resolved(id, descriptor, changing, moduleSource);
    }

    public void resolved(ModuleVersionIdentifier id, ModuleDescriptor descriptor, boolean changing, ModuleSource moduleSource) {
        reset(State.Resolved);
        this.moduleVersionIdentifier = id;
        this.moduleDescriptor = descriptor;
        this.changing = changing;
        this.moduleSource = moduleSource;
        this.status = moduleDescriptor.getStatus();
    }

    public void missing() {
        reset(State.Missing);
    }

    public void probablyMissing() {
        reset(State.ProbablyMissing);
    }

    public void failed(ModuleVersionResolveException failure) {
        reset(State.Failed);
        this.failure = failure;
    }

    public State getState() {
        return state;
    }

    public ModuleVersionResolveException getFailure() {
        assertHasResult();
        return failure;
    }

    public MutableModuleVersionMetaData getMetaData() throws ModuleVersionResolveException {
        assertResolved();
        return this;
    }

    private void assertHasResult() {
        if (state == State.Unknown) {
            throw new IllegalStateException("No result has been specified.");
        }
    }

    private void assertResolved() {
        if (state == State.Failed) {
            throw failure;
        }
        if (state != State.Resolved) {
            throw new IllegalStateException("This module has not been resolved.");
        }
    }

    public ModuleVersionIdentifier getId() {
        assertResolved();
        return moduleVersionIdentifier;
    }

    public ModuleDescriptor getDescriptor() {
        assertResolved();
        return moduleDescriptor;
    }

    public List<DependencyMetaData> getDependencies() {
        assertResolved();
        if (dependencies == null) {
            dependencies = new ArrayList<DependencyMetaData>();
            for (final DependencyDescriptor dependencyDescriptor : moduleDescriptor.getDependencies()) {
                dependencies.add(new DefaultDependencyMetaData(dependencyDescriptor));
            }
        }
        return dependencies;
    }

    public void setDependencies(Iterable<? extends DependencyMetaData> dependencies) {
        assertResolved();
        this.dependencies = CollectionUtils.toList(dependencies);
        for (DefaultConfigurationMetaData configuration : configurations.values()) {
            configuration.dependencies = null;
        }
    }

    public DefaultConfigurationMetaData getConfiguration(final String name) {
        assertResolved();
        DefaultConfigurationMetaData configuration = configurations.get(name);
        if (configuration == null) {
            Configuration descriptor = moduleDescriptor.getConfiguration(name);
            if (descriptor == null) {
                return null;
            }
            Set<String> hierarchy = new LinkedHashSet<String>();
            hierarchy.add(name);
            for (String parent : descriptor.getExtends()) {
                hierarchy.addAll(getConfiguration(parent).hierarchy);
            }
            configuration = new DefaultConfigurationMetaData(descriptor, hierarchy);
            configurations.put(name, configuration);
        }
        return configuration;
    }

    public boolean isChanging() {
        assertResolved();
        return changing;
    }

    public ModuleSource getModuleSource() {
        assertResolved();
        return moduleSource;
    }

    public void setModuleSource(ModuleSource moduleSource) {
        assertResolved();
        this.moduleSource = moduleSource;
    }

    public String getStatus() {
        assertResolved();
        return status;
    }

    public List<String> getStatusScheme() {
        assertResolved();
        return statusScheme;
    }

    public void setChanging(boolean changing) {
        this.changing = changing;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setStatusScheme(List<String> statusScheme) {
        this.statusScheme = statusScheme;
    }

    private class DefaultConfigurationMetaData implements ConfigurationMetaData {
        private final Configuration descriptor;
        private final Set<String> hierarchy;
        private List<DependencyMetaData> dependencies;
        private Set<Artifact> artifacts;
        private LinkedHashSet<ExcludeRule> excludeRules;

        private DefaultConfigurationMetaData(Configuration descriptor, Set<String> hierarchy) {
            this.descriptor = descriptor;
            this.hierarchy = hierarchy;
        }

        public ModuleVersionMetaData getModuleVersion() {
            return DefaultBuildableModuleVersionMetaDataResolveResult.this;
        }

        public String getName() {
            return descriptor.getName();
        }

        public Set<String> getHierarchy() {
            return hierarchy;
        }

        public boolean isTransitive() {
            return descriptor.isTransitive();
        }

        public List<DependencyMetaData> getDependencies() {
            if (dependencies == null) {
                dependencies = new ArrayList<DependencyMetaData>();
                for (DependencyMetaData dependency : DefaultBuildableModuleVersionMetaDataResolveResult.this.getDependencies()) {
                    if (include(dependency)) {
                        dependencies.add(dependency);
                    }
                }
            }
            return dependencies;
        }

        private boolean include(DependencyMetaData dependency) {
            String[] moduleConfigurations = dependency.getDescriptor().getModuleConfigurations();
            for (int i = 0; i < moduleConfigurations.length; i++) {
                String moduleConfiguration = moduleConfigurations[i];
                if (moduleConfiguration.equals("%") || hierarchy.contains(moduleConfiguration)) {
                    return true;
                }
                if (moduleConfiguration.equals("*")) {
                    boolean include = true;
                    for (int j = i + 1; j < moduleConfigurations.length && moduleConfigurations[j].startsWith("!"); j++) {
                        if (moduleConfigurations[j].substring(1).equals(getName())) {
                            include = false;
                            break;
                        }
                    }
                    if (include) {
                        return true;
                    }
                }
            }
            return false;
        }

        public Set<ExcludeRule> getExcludeRules() {
            if (excludeRules == null) {
                excludeRules = new LinkedHashSet<ExcludeRule>();
                for (ExcludeRule excludeRule : moduleDescriptor.getAllExcludeRules()) {
                    for (String config : excludeRule.getConfigurations()) {
                        if (hierarchy.contains(config)) {
                            excludeRules.add(excludeRule);
                            break;
                        }
                    }
                }
            }
            return excludeRules;
        }

        public Set<Artifact> getArtifacts() {
            if (artifacts == null) {
                artifacts = new LinkedHashSet<Artifact>();
                for (String ancestor : hierarchy) {
                    for (Artifact artifact : moduleDescriptor.getArtifacts(ancestor)) {
                        artifacts.add(artifact);
                    }
                }
            }
            return artifacts;
        }
    }
}

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

import org.apache.ivy.core.module.descriptor.Artifact;
import org.gradle.api.internal.artifacts.ivyservice.BuildableArtifactResolveResult;
import org.gradle.api.internal.artifacts.repositories.resolver.ExternalResourceResolver;

/**
 * A {@link ModuleVersionRepository} wrapper around an {@link ExternalResourceResolver}.
 */
public class ExternalResourceResolverAdapter extends AbstractDependencyResolverAdapter {
    private final ExternalResourceResolver resolver;
    private final boolean dynamicResolve;

    public ExternalResourceResolverAdapter(ExternalResourceResolver resolver, boolean dynamicResolve) {
        super(resolver);
        this.resolver = resolver;
        this.dynamicResolve = dynamicResolve;
    }

    @Override
    public boolean isDynamicResolveMode() {
        return dynamicResolve;
    }

    public void getDependency(DependencyMetaData dependency, BuildableModuleVersionMetaData result) {
        resolver.getDependency(dependency.getDescriptor(), result);
    }

    public void resolve(Artifact artifact, BuildableArtifactResolveResult result, ModuleSource moduleSource) {
        resolver.resolve(artifact, result, moduleSource);
    }
}

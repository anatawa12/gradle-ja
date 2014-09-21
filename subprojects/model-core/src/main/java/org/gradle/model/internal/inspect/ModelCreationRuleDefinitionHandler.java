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

package org.gradle.model.internal.inspect;

import org.gradle.model.InvalidModelRuleDeclarationException;
import org.gradle.model.Model;
import org.gradle.model.internal.core.*;
import org.gradle.model.internal.core.rule.describe.ModelRuleDescriptor;
import org.gradle.model.internal.registry.ModelRegistry;

import java.util.List;

public class ModelCreationRuleDefinitionHandler extends AbstractAnnotationDrivenMethodRuleDefinitionHandler<Model> {
    public <T> void register(MethodRuleDefinition<T> ruleDefinition, ModelRegistry modelRegistry, RuleSourceDependencies dependencies) {

        // TODO validate model name
        String modelName = determineModelName(ruleDefinition);

        try {
            ModelPath.validatePath(modelName);
        } catch (Exception e) {
            throw new InvalidModelRuleDeclarationException(String.format("Path of declared model element created by rule %s is invalid.", ruleDefinition.getDescriptor()), e);
        }

        // TODO validate the return type (generics?)
        ModelType<T> returnType = ruleDefinition.getReturnType();
        ModelPath path = ModelPath.path(modelName);
        List<ModelReference<?>> references = ruleDefinition.getReferences();

        modelRegistry.create(new MethodModelCreator<T>(returnType, path, references, ruleDefinition.getRuleInvoker(), ruleDefinition.getDescriptor()));
    }

    private String determineModelName(MethodRuleDefinition<?> ruleDefinition) {
        String annotationValue = ruleDefinition.getAnnotation(Model.class).value();
        if (annotationValue == null || annotationValue.isEmpty()) {
            return ruleDefinition.getMethodName();
        } else {
            return annotationValue;
        }
    }

    private static class MethodModelCreator<R> implements ModelCreator {
        private final ModelType<R> type;
        private final ModelPath path;
        private final ModelPromise promise;
        private final ModelRuleDescriptor descriptor;
        private List<ModelReference<?>> inputs;
        private final ModelRuleInvoker<R> ruleInvoker;

        public MethodModelCreator(ModelType<R> type, ModelPath path, List<ModelReference<?>> inputs, ModelRuleInvoker<R> ruleInvoker, ModelRuleDescriptor descriptor) {
            this.type = type;
            this.path = path;
            this.inputs = inputs;
            this.promise = new SingleTypeModelPromise(type);
            this.ruleInvoker = ruleInvoker;
            this.descriptor = descriptor;
        }

        public ModelPath getPath() {
            return path;
        }

        public ModelPromise getPromise() {
            return promise;
        }

        public List<ModelReference<?>> getInputs() {
            return inputs;
        }

        public ModelAdapter create(Inputs inputs) {
            R instance = invoke(inputs);
            if (instance == null) {
                throw new ModelRuleExecutionException(getDescriptor(), "rule returned null");
            }

            return InstanceModelAdapter.of(type, instance);
        }

        @SuppressWarnings("unchecked")
        private R invoke(Inputs inputs) {
            if (inputs.size() == 0) {
                return ruleInvoker.invoke();
            } else {
                Object[] args = new Object[inputs.size()];
                for (int i = 0; i < inputs.size(); i++) {
                    args[i] = inputs.get(i, this.inputs.get(i).getType()).getInstance();
                }

                return ruleInvoker.invoke(args);
            }
        }

        public ModelRuleDescriptor getDescriptor() {
            return descriptor;
        }
    }
}

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

package org.gradle.nativecode.toolchain.internal.msvcpp;

import org.gradle.api.internal.tasks.compile.Compiler;
import org.gradle.nativecode.base.internal.*;

// TODO:DAZ Merge with DefaultToolChainRegistry.UnavailableToolChain
public class UnavailableToolChain implements ToolChainInternal {
    public String getName() {
        return VisualCppToolChain.NAME;
    }

    @Override
    public String toString() {
        return "Visual C++";
    }

    public ToolChainAvailability getAvailability() {
        return new ToolChainAvailability().unavailable("Visual C++ is not available on this operating system.");
    }

    public <T extends BinaryToolSpec> Compiler<T> createCppCompiler() {
        throw new UnsupportedOperationException();
    }

    public <T extends BinaryToolSpec> Compiler<T> createCCompiler() {
        throw new UnsupportedOperationException();
    }

    public <T extends BinaryToolSpec> Compiler<T> createAssembler() {
        throw new UnsupportedOperationException();
    }

    public <T extends LinkerSpec> Compiler<T> createLinker() {
        throw new UnsupportedOperationException();
    }

    public <T extends StaticLibraryArchiverSpec> Compiler<T> createStaticLibraryArchiver() {
        throw new UnsupportedOperationException();
    }

    public String getExecutableName(String executablePath) {
        throw new UnsupportedOperationException();
    }

    public String getSharedLibraryName(String libraryName) {
        throw new UnsupportedOperationException();
    }

    public String getSharedLibraryLinkFileName(String libraryPath) {
        throw new UnsupportedOperationException();
    }

    public String getStaticLibraryName(String libraryName) {
        throw new UnsupportedOperationException();
    }

    public String getOutputType() {
        throw new UnsupportedOperationException();
    }
}

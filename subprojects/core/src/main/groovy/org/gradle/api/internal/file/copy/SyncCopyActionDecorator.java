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
package org.gradle.api.internal.file.copy;

import org.gradle.api.Action;
import org.gradle.api.file.FileVisitDetails;
import org.gradle.api.file.FileVisitor;
import org.gradle.api.file.RelativePath;
import org.gradle.api.internal.file.collections.DirectoryFileTree;
import org.gradle.api.internal.file.collections.MinimalFileTree;
import org.gradle.api.internal.tasks.SimpleWorkResult;
import org.gradle.api.tasks.WorkResult;
import org.gradle.util.GFileUtils;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class SyncCopyActionDecorator implements CopyAction {
    private final File baseDestDir;
    private final CopyAction delegate;

    public SyncCopyActionDecorator(File baseDestDir, CopyAction delegate) {
        this.baseDestDir = baseDestDir;
        this.delegate = delegate;
    }

    public WorkResult execute(final CopyActionProcessingStream stream) {
        final Set<RelativePath> visited = new HashSet<RelativePath>();

        WorkResult didWork = delegate.execute(new CopyActionProcessingStream() {
            public void process(final Action<? super FileCopyDetailsInternal> action) {
                stream.process(new Action<FileCopyDetailsInternal>() {
                    public void execute(FileCopyDetailsInternal details) {
                        visited.add(details.getRelativePath());
                        action.execute(details);
                    }
                });
            }
        });

        final BooleanHolder didDeleteHolder = new BooleanHolder();
        FileVisitor fileVisitor = new FileVisitor() {
            public void visitDir(FileVisitDetails dirDetails) {
                maybeDelete(dirDetails, true);
            }

            public void visitFile(FileVisitDetails fileDetails) {
                maybeDelete(fileDetails, false);
            }

            private void maybeDelete(FileVisitDetails fileDetails, boolean isDir) {
                RelativePath path = fileDetails.getRelativePath();
                if (!visited.contains(path)) {
                    if (isDir) {
                        GFileUtils.deleteDirectory(fileDetails.getFile());
                    } else {
                        GFileUtils.deleteQuietly(fileDetails.getFile());
                    }
                    didDeleteHolder.flag = true;
                }
            }
        };

        MinimalFileTree walker = new DirectoryFileTree(baseDestDir).postfix();
        walker.visit(fileVisitor);
        visited.clear();

        return new SimpleWorkResult(didWork.getDidWork() || didDeleteHolder.flag);
    }

    private static class BooleanHolder {
        boolean flag;
    }

}

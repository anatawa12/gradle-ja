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
package org.gradle.api.internal.filestore;

import org.gradle.api.Action;
import org.gradle.api.Transformer;
import org.gradle.util.hash.HashUtil;

import java.io.File;
import java.util.Random;
import java.util.Set;

public class GroupedAndNamedUniqueFileStore<K> implements FileStore<K>, FileStoreSearcher<K> {

    private final Random generator = new Random(System.currentTimeMillis());

    private PathKeyFileStore delegate;
    private final Transformer<String, K> grouper;
    private final Transformer<String, K> namer;


    public GroupedAndNamedUniqueFileStore(PathKeyFileStore delegate, Transformer<String, K> grouper, Transformer<String, K> namer) {
        this.delegate = delegate;
        this.grouper = grouper;
        this.namer = namer;
    }

    public FileStoreEntry move(K key, File source) {
        return delegate.move(toPath(key, getChecksum(source)), source);
    }

    public FileStoreEntry copy(K key, File source) {
        return delegate.copy(toPath(key, getChecksum(source)), source);
    }

    public Set<? extends FileStoreEntry> search(K key) {
        return delegate.search(toPath(key, "*"));
    }

    protected String toPath(K key, String checksumPart) {
        String group = grouper.transform(key);
        String name = namer.transform(key);

        return String.format("%s/%s/%s", group, checksumPart, name);
    }

    private String getChecksum(File contentFile) {
        return HashUtil.createHash(contentFile, "SHA1").asHexString();
    }

    public File getTempFile() {
        long tempLong = generator.nextLong();
        tempLong = tempLong < 0 ? -tempLong : tempLong;
        return new File(delegate.getBaseDir(), "temp/" + tempLong);
    }

    public void moveFilestore(File destination) {
        delegate.moveFilestore(destination);
    }

    public FileStoreEntry add(K key, Action<File> addAction) {
        //We cannot just delegate to the add method as we need the file content for checksum calculation here
        //and reexecuting the action isn't acceptable
        final File tempFile = getTempFile();
        addAction.execute(tempFile);
        final String groupedAndNamedKey = toPath(key, getChecksum(tempFile));
        final FileStoreEntry fileStoreEntry = delegate.move(groupedAndNamedKey, tempFile);
        return fileStoreEntry;
    }
}

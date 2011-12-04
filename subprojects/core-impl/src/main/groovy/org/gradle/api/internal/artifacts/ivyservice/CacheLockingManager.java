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
package org.gradle.api.internal.artifacts.ivyservice;

import net.jcip.annotations.ThreadSafe;
import org.gradle.api.internal.Factory;
import org.gradle.cache.PersistentIndexedCache;

import java.io.File;

@ThreadSafe
public interface CacheLockingManager {
    /**
     * Performs some work against the cache. Acquires exclusive locks the appropriate resources, so that the given action is the only
     * action to execute across all processes (including this one).
     *
     * <p>This method is re-entrant.</p>
     */
    <T> T useCache(String operationDisplayName, Factory<? extends T> action);

    /**
     * Performs some long running operation within an action invoked by {@link #useCache(String, org.gradle.api.internal.Factory)}. Releases all
     * locks while the operation is running, and then reacquires the locks.
     *
     * <p>This method is re-entrant.</p>
     */
    <T> T longRunningOperation(String operationDisplayName, Factory<? extends T> action);

    /**
     * Performs some long running operation within an action invoked by {@link #useCache(String, org.gradle.api.internal.Factory)}. Releases all
     * locks while the operation is running, and then reacquires the locks.
     *
     * <p>This method is re-entrant.</p>
     */
    void longRunningOperation(String operationDisplayName, Runnable action);

    /**
     * Creates a cache implementation that is managed by this locking manager.
     */
    <K, V> PersistentIndexedCache<K, V> createCache(File cacheFile, Class<K> keyType, Class<V> valueType);
}

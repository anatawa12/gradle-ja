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
package org.gradle.api.internal.artifacts.ivyservice

import org.gradle.cache.CacheRepository
import org.gradle.cache.DirectoryCacheBuilder
import org.gradle.cache.PersistentCache
import org.jfrog.wharf.ivy.lock.LockHolderFactory
import spock.lang.Specification
import org.gradle.cache.CacheBuilder

class ArtifactCacheMetaDataTest extends Specification {
    final File cacheDir = new File('user-dir')
    final CacheRepository cacheRepository = Mock()
    final DirectoryCacheBuilder cacheBuilder = Mock()
    final PersistentCache cache = Mock()
    final LockHolderFactory lockHolderFactory = Mock()
    final ArtifactCacheMetaData metaData = new ArtifactCacheMetaData(cacheRepository)

    def "creates and configures an IvySettings instance"() {
        given:
        _ * cache.baseDir >> cacheDir

        when:
        def result = metaData.cacheDir

        then:
        result == cacheDir

        and:
        1 * cacheRepository.store("artifacts-4") >> cacheBuilder
        1 * cacheBuilder.withVersionStrategy(CacheBuilder.VersionStrategy.SharedCache) >> cacheBuilder
        1 * cacheBuilder.open() >> cache
        0 * cacheRepository._
    }

}

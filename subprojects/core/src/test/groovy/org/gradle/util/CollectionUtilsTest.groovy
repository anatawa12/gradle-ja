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
package org.gradle.util

import org.gradle.api.Transformer
import org.gradle.api.specs.Spec
import org.gradle.api.specs.Specs
import spock.lang.Specification
import spock.lang.Unroll

import static org.gradle.util.CollectionUtils.*

class CollectionUtilsTest extends Specification {

    def "list filtering"() {
        given:
        def spec = Specs.convertClosureToSpec { it < 5 }
        def filter = { Integer[] nums -> filter(nums as List, spec) }

        expect:
        filter(1, 2, 3) == [1, 2, 3]
        filter(7, 8, 9) == []
        filter() == []
        filter(4, 5, 6) == [4]
    }

    def "list collecting"() {
        def transformer = new Transformer() {
            def transform(i) { i * 2 }
        }
        def collect = { Integer[] nums -> collect(nums as List, transformer) }

        expect:
        collect(1, 2, 3) == [2, 4, 6]
        collect() == []
    }

    def "set filtering"() {
        given:
        def spec = Specs.convertClosureToSpec { it < 5 }
        def filter = { Integer[] nums -> filter(nums as Set, spec) }

        expect:
        filter(1, 2, 3) == [1, 2, 3] as Set
        filter(7, 8, 9).empty
        filter().empty
        filter(4, 5, 6) == [4] as Set
    }

    def "map filtering"() {
        expect:
        def filtered = filter(a: 1, b: 2, c: 3, spec { it.value < 2 })
        filtered.size() == 1
        filtered.a == 1
    }

    def toStringList() {
        def list = [42, "string"]

        expect:
        toStringList([]) == []
        toStringList(list) == ["42", "string"]
    }

    def "list compacting"() {
        expect:
        compact([1, null, 2]) == [1, 2]
        compact([null, 1, 2]) == [1, 2]
        compact([1, 2, null]) == [1, 2]

        def l = [1, 2, 3]
        compact(l).is l

    }

    def "list stringize"() {
        expect:
        stringize([1, 2, 3]) == ["1", "2", "3"]
        stringize([]) == []
    }

    def "stringize"() {
        expect:
        stringize(["c", "b", "a"], new TreeSet<String>()) == ["a", "b", "c"] as Set
    }

    def "replacing"() {
        given:
        def l = [1, 2, 3]

        expect:
        replace l, spec { it == 2 }, transformer { 2 * 2 }
        l == [1, 4, 3]

        replace l, spec { it > 1 }, transformer { 0 }
        l == [1, 0, 0]

        !replace(l, spec { false }, transformer { it })
    }

    @Unroll
    "diffing sets"() {
        given:
        def leftSet = left as Set
        def rightSet = right as Set
        def leftOnlySet = leftOnly as Set
        def rightOnlySet = rightOnly as Set

        when:
        def diff = diffSetsBy(leftSet, rightSet, transformer { it + 10 })

        then:
        diff.leftOnly == leftOnlySet
        diff.common.size() == common.size()
        if (common) {
            common.each { inCommon ->
                diff.common.find { it.left == inCommon && it.right == inCommon }
            }
        }
        diff.rightOnly == rightOnlySet

        where:
        left      | right     | leftOnly  | rightOnly | common
        [1, 2, 3] | [2, 3, 4] | [1]       | [4]       | [2, 3]
        []        | []        | []        | []        | []
        [1, 2, 3] | []        | [1, 2, 3] | []        | []
        []        | [1, 2, 3] | []        | [1, 2, 3] | []
        [1, 2, 3] | [1, 2, 3] | []        | []        | [1, 2, 3]
    }

    def "collect as map"() {
        expect:
        collectMap([1, 2, 3], transformer { it * 10 }) == [10: 1, 20: 2, 30: 3]
        collectMap([], transformer { it * 10 }) == [:]
    }

    def "every"() {
        expect:
        every([1, 2, 3], spec { it < 4 })
        !every([1, 2, 4], spec { it < 4 })
        !every([1], spec { it instanceof String })
        every([], spec { false })
    }

    Spec<?> spec(Closure c) {
        Specs.convertClosureToSpec(c)
    }

    Transformer<?, ?> transformer(Closure c) {
        c as Transformer
    }
}
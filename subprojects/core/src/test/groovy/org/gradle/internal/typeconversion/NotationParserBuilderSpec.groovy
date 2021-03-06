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

package org.gradle.internal.typeconversion

import spock.lang.Specification

import static org.gradle.util.TextUtil.toPlatformLineSeparators

class NotationParserBuilderSpec extends Specification {

    def "adds just return parser as default"() {
        when:
        def parser = NotationParserBuilder.toType(String.class).toComposite()

        then:
        "Some String" == parser.parseNotation("Some String")
    }

    def "can add a converter"() {
        def converter = Mock(NotationConverter)
        given:
        converter.convert(_, _) >> { Object n, NotationConvertResult result -> result.converted("[${n}]") }

        and:
        def parser = NotationParserBuilder.toType(String.class).converter(converter).toComposite()

        expect:
        parser.parseNotation(12) == "[12]"
    }

    def "can add a converter that converts notations of a given type"() {
        def converter = Mock(NotationConverter)

        given:
        converter.convert(_, _) >> { Number n, NotationConvertResult result -> result.converted("[${n}]") }

        and:
        def parser = NotationParserBuilder.toType(String.class).fromType(Number, converter).toComposite()

        expect:
        parser.parseNotation(12) == "[12]"
    }

    def "can add a converter that converts CharSequence notations"() {
        def converter = Mock(NotationConverter)

        given:
        converter.convert(_, _) >> { String n, NotationConvertResult result -> result.converted("[${n}]") }

        and:
        def parser = NotationParserBuilder.toType(String.class).fromCharSequence(converter).toComposite()

        expect:
        parser.parseNotation(new StringBuilder("12")) == "[12]"
    }

    def "can add a converter that converts CharSequence notations when the target type is String"() {
        def parser = NotationParserBuilder.toType(String.class).fromCharSequence().toComposite()

        expect:
        parser.parseNotation(new StringBuilder("12")) == "12"
        parser.parseNotation("12") == "12"
    }

    def "can add a parser"() {
        def target = Mock(NotationParser)
        given:
        target.parseNotation(_) >> { Number n -> return "[${n}]" }

        and:
        def parser = NotationParserBuilder.toType(String.class).parser(target).toComposite()

        expect:
        parser.parseNotation(12) == "[12]"
    }

    def "can opt in to allow null as input"() {
        def converter = Mock(NotationConverter)
        def parser = NotationParserBuilder
                .toType(String.class)
                .allowNullInput()
                .converter(converter)
                .toComposite()


        given:
        converter.convert(null, _) >> { Object n, NotationConvertResult result -> result.converted("[null]") }

        expect:
        parser.parseNotation(null) == "[null]"
    }

    def "can tweak the conversion error messages"() {
        given:
        def parser = NotationParserBuilder.toType(String.class).typeDisplayName("a thing").toComposite()

        when:
        parser.parseNotation(12)

        then:
        UnsupportedNotationException e = thrown()
        e.message == toPlatformLineSeparators("""Cannot convert the provided notation to a thing: 12.
The following types/formats are supported:
  - Instances of String.""")
    }

    def "uses nice display name for String target type"() {
        given:
        def parser = NotationParserBuilder.toType(String.class).toComposite()

        when:
        parser.parseNotation(12)

        then:
        UnsupportedNotationException e = thrown()
        e.message == toPlatformLineSeparators("""Cannot convert the provided notation to a String: 12.
The following types/formats are supported:
  - Instances of String.""")
    }
}

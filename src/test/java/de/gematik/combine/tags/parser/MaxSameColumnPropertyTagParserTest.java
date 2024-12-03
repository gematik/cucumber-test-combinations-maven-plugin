/*
 * Copyright 2023 gematik GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.gematik.combine.tags.parser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import de.gematik.combine.filter.table.MaxSameColumnPropertyFilter;
import de.gematik.combine.tags.ParsedTags;
import de.gematik.combine.tags.TagParser.PreParsedTag;
import java.util.List;
import java.util.stream.Stream;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class MaxSameColumnPropertyTagParserTest {

  MaxSameColumnPropertyTagParser parser = new MaxSameColumnPropertyTagParser();
  ParsedTags tagCollector;

  @BeforeEach
  void beforeEach() {
    tagCollector = new ParsedTags(List.of("A"));
  }

  public static Stream<Arguments> invalidValues() {
    return Stream.of(
        arguments(""), arguments("A"), arguments("A,b"), arguments("A,b,"), arguments("A,b,c,d"));
  }

  @ParameterizedTest
  @MethodSource("invalidValues")
  @SneakyThrows
  void shouldRejectInvalidValues(String value) {
    // arrange
    PreParsedTag preParsedTag = new PreParsedTag("TestName", value);
    // act
    assertThatThrownBy(() -> parser.parseTagAndRegister(preParsedTag, tagCollector))
        // assert
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("'" + value + "' does not have exact 3 arguments");
  }

  @Test
  @SneakyThrows
  void shouldRejectInvalidCountParam() {
    // arrange
    PreParsedTag preParsedTag = new PreParsedTag("TestName", "A,b,c");
    // act
    assertThatThrownBy(() -> parser.parseTagAndRegister(preParsedTag, tagCollector))
        // assert
        .isInstanceOf(NumberFormatException.class);
  }

  @Test
  @SneakyThrows
  void shouldParseParams() {
    // act
    parser.parseTagAndRegister(new PreParsedTag("TestName", "A,b,2"), tagCollector);
    // assert
    assertThat(tagCollector.getTableFilters())
        .isEqualTo(List.of(new MaxSameColumnPropertyFilter("A", "b", 2)));
  }
}

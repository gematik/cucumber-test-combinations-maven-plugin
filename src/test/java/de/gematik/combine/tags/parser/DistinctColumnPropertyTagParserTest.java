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

import static de.gematik.combine.tags.parser.DistinctColumnPropertyTagParser.DISTINCT_COLUMN_PROPERTY_TAG;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import de.gematik.combine.tags.ParsedTags;
import de.gematik.combine.tags.TagParser.PreParsedTag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DistinctColumnPropertyTagParserTest {

  private final static DistinctColumnPropertyTagParser parser = new DistinctColumnPropertyTagParser();

  @Mock
  ParsedTags parsedTags;

  @ParameterizedTest
  @CsvSource(delimiter = '|', value = {"Header_1", "someValue,AnotherValue,additionalValue"})
  void shouldThrowIllegalArgumentExceptionIfNot2ArgumentsDelivered(String value) {
    // arrange
    PreParsedTag preParsedTag = new PreParsedTag(DISTINCT_COLUMN_PROPERTY_TAG, value);
    // act
    assertThatThrownBy(() -> parser.parseTagAndRegister(preParsedTag, null))
        // assert
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void shouldParseSuccessfully() {
    // arrange
    PreParsedTag preParsedTag = new PreParsedTag(DISTINCT_COLUMN_PROPERTY_TAG, "Header_1,value");
    // act
    parser.parseTagAndRegister(preParsedTag, parsedTags);
    // assert
    verify(parsedTags, times(1)).addTableFilter(any());
  }
}
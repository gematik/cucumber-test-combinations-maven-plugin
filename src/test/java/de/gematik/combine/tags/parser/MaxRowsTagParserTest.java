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

import de.gematik.combine.FilterConfiguration;
import de.gematik.combine.tags.ConfigModifier;
import de.gematik.combine.tags.ParsedTags;
import de.gematik.combine.tags.TagParser;
import de.gematik.combine.tags.TagParser.PreParsedTag;
import java.util.List;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MaxRowsTagParserTest {

  MaxRowsTagParser parser = new MaxRowsTagParser();
  ParsedTags tagCollector;

  @BeforeEach
  void beforeEach() {
    tagCollector = new ParsedTags(List.of("A"));
  }

  @Test
  @SneakyThrows
  void shouldSetConfigField() {
    // arrange
    FilterConfiguration config = FilterConfiguration.builder().build();
    // act
    parser.parseTagAndRegister(new PreParsedTag("TestName","1"), tagCollector);
    // assert
    List<ConfigModifier> configModifiers = tagCollector.getConfigModifiers();
    assertThat(configModifiers).hasSize(1)
        .map(configurationModifier -> configurationModifier.apply(config))
        .element(0)
        .isEqualTo(config.toBuilder().maxTableRows(1).build());
  }
}

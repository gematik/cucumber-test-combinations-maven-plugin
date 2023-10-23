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

import static de.gematik.combine.util.CompareOperator.OPERATOR_FORMAT;
import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;

import de.gematik.combine.filter.table.cell.CellFilter;
import de.gematik.combine.filter.table.cell.VersionFilter;
import de.gematik.combine.model.properties.Version;
import de.gematik.combine.tags.ParsedTags;
import de.gematik.combine.tags.TagParser.PreParsedTag;
import de.gematik.combine.util.CompareOperator;
import java.util.LinkedList;
import java.util.List;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class VersionFilterParserTest {

  public static final String HEADER = "a";
  public static final Version VERSION = new Version(new LinkedList<>(List.of(1, 1, 1)));
  public static final VersionFilterParser PARSER = new VersionFilterParser();
  public static final String TAG_VALUE_TO_PARSE = format("%s%s%s", HEADER, "%s", VERSION);

  @ParameterizedTest
  @EnumSource(value = CompareOperator.class)
  void shouldParseTagAndRegister(CompareOperator operator) {
    // arrange
    ParsedTags parsedTags = new ParsedTags(List.of(HEADER));
    String valueToParse = format(TAG_VALUE_TO_PARSE, format(OPERATOR_FORMAT, operator.name()));

    // act
    PARSER.parseTagAndRegister(new PreParsedTag("TestName", valueToParse), parsedTags);

    // assert
    assertThat(parsedTags.getCellFilters()).hasSize(1);

    CellFilter cellFilter = parsedTags.getCellFilters().get(HEADER).get(0);
    assertThat(cellFilter).isInstanceOf(VersionFilter.class);

    VersionFilter versionFilter = (VersionFilter) cellFilter;
    assertThat(versionFilter.getFilterVersion()).isEqualTo(VERSION);
    assertThat(versionFilter.getOperator()).isEqualTo(operator);
  }

}
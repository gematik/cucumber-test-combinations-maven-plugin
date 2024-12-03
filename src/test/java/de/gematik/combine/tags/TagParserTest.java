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

package de.gematik.combine.tags;

import static de.gematik.combine.DependencyInstances.ALL_PARSERS;
import static de.gematik.combine.tags.parser.AllowDoubleLineupTagParser.ALLOW_DOUBLE_LINEUP_TAG;
import static de.gematik.combine.tags.parser.AllowSelfCombineTagParser.ALLOW_SELF_COMBINE_TAG;
import static de.gematik.combine.tags.parser.DistinctColumnPropertyTagParser.DISTINCT_COLUMN_PROPERTY_TAG;
import static de.gematik.combine.tags.parser.DistinctColumnTagParser.DISTINCT_COLUMN_TAG;
import static de.gematik.combine.tags.parser.DistinctRowPropertyTagParser.DISTINCT_ROW_PROPERTY_TAG;
import static de.gematik.combine.tags.parser.EqualRowPropertyTagParser.EQUAL_ROW_PROPERTY_TAG;
import static de.gematik.combine.tags.parser.FilterTagParser.JEXL_ROW_FILTER_TAG;
import static de.gematik.combine.tags.parser.MaxRowsTagParser.MAX_ROWS_TAG;
import static de.gematik.combine.tags.parser.MaxSameColumnPropertyTagParser.MAX_SAME_COLUMN_PROPERTY_TAG;
import static de.gematik.combine.tags.parser.MinimalTableTagParser.MINIMAL_TABLE_TAG;
import static de.gematik.combine.tags.parser.RequirePropertyTagParser.REQUIRE_PROPERTY_TAG;
import static de.gematik.combine.tags.parser.RequireTagTagParser.REQUIRE_TAG_TAG;
import static de.gematik.combine.tags.parser.ShuffleTagParser.SHUFFLE_TAG;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import de.gematik.combine.filter.table.DistinctColumnFilter;
import de.gematik.combine.filter.table.MaxRowsFilter;
import de.gematik.combine.filter.table.MaxSameColumnPropertyFilter;
import de.gematik.combine.filter.table.TableFilter;
import de.gematik.combine.filter.table.cell.CellFilter;
import de.gematik.combine.filter.table.cell.JexlCellFilter;
import de.gematik.combine.filter.table.row.DistinctRowPropertyFilter;
import de.gematik.combine.filter.table.row.EqualRowPropertyFilter;
import de.gematik.combine.filter.table.row.JexlRowFilter;
import de.gematik.combine.filter.table.row.RequirePropertyRowFilter;
import de.gematik.combine.filter.table.row.RequireTagRowFilter;
import de.gematik.combine.filter.table.row.TableRowFilter;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class TagParserTest {

  List<String> columns = List.of("A", "B");

  TagParser tagParser = new TagParser(ALL_PARSERS);

  public static Stream<Arguments> configurationTags() {
    return Stream.of(
        arguments("@" + MAX_ROWS_TAG + "(1)"),
        arguments("@" + SHUFFLE_TAG),
        arguments("@" + SHUFFLE_TAG + "(true)"),
        arguments("@" + SHUFFLE_TAG + "(false)"),
        arguments("@" + MINIMAL_TABLE_TAG),
        arguments("@" + MINIMAL_TABLE_TAG + "(true)"),
        arguments("@" + MINIMAL_TABLE_TAG + "(false)"),
        arguments("@" + ALLOW_SELF_COMBINE_TAG),
        arguments("@" + ALLOW_SELF_COMBINE_TAG + "(true)"),
        arguments("@" + ALLOW_SELF_COMBINE_TAG + "(false)"),
        arguments("@" + ALLOW_DOUBLE_LINEUP_TAG),
        arguments("@" + ALLOW_DOUBLE_LINEUP_TAG + "(true)"),
        arguments("@" + ALLOW_DOUBLE_LINEUP_TAG + "(false)"));
  }

  @ParameterizedTest
  @MethodSource("configurationTags")
  void shouldParseConfigurationTags(String tagString) {
    // act
    ParsedTags tagCollector = tagParser.parseTags(List.of(tagString), columns);
    // assert
    assertThat(tagCollector.getConfigModifiers())
        .hasSize(1)
        .element(0)
        .isInstanceOf(ConfigModifier.class);
  }

  public static Stream<Arguments> tableFilterTags() {
    return Stream.of(
        arguments("@" + DISTINCT_COLUMN_TAG + "(A)", new DistinctColumnFilter("A")),
        arguments(
            "@" + DISTINCT_COLUMN_PROPERTY_TAG + "(A,prop1)",
            new MaxSameColumnPropertyFilter("A", "prop1", 1)),
        arguments("@" + MAX_ROWS_TAG + "(columnCount/2)", new MaxRowsFilter("columnCount/2")),
        arguments(
            "@" + MAX_SAME_COLUMN_PROPERTY_TAG + "(A,prop1,2)",
            new MaxSameColumnPropertyFilter("A", "prop1", 2)));
  }

  @ParameterizedTest
  @MethodSource("tableFilterTags")
  void shouldParseTableFilterTags(String tagString, TableFilter expectedTag) {
    // act
    ParsedTags tagCollector = tagParser.parseTags(List.of(tagString), columns);
    // assert
    assertThat(tagCollector.getTableFilters()).isEqualTo(List.of(expectedTag));
  }

  public static Stream<Arguments> tableRowFilterTags() {
    return Stream.of(
        arguments("@" + REQUIRE_TAG_TAG + "(tag1)", new RequireTagRowFilter("tag1")),
        arguments(
            "@" + REQUIRE_PROPERTY_TAG + "(prop1,val)",
            new RequirePropertyRowFilter("prop1", "val")),
        arguments(
            "@" + DISTINCT_ROW_PROPERTY_TAG + "(prop1)", new DistinctRowPropertyFilter("prop1")),
        arguments("@" + EQUAL_ROW_PROPERTY_TAG + "(prop1)", new EqualRowPropertyFilter("prop1")),
        arguments(
            "@" + JEXL_ROW_FILTER_TAG + "(A.hasTag(\"orgAdmin\")&&B.hasTag(\"orgAdmin\"))",
            new JexlRowFilter("A.hasTag(\"orgAdmin\")&&B.hasTag(\"orgAdmin\")")));
  }

  @ParameterizedTest
  @MethodSource("tableRowFilterTags")
  void shouldParseTableRowFilterTags(String tagString, TableRowFilter expectedTag) {
    // act
    ParsedTags tagCollector = tagParser.parseTags(List.of(tagString), columns);
    // assert
    assertThat(tagCollector.getTableRowFilters()).isEqualTo(List.of(expectedTag));
  }

  public static Stream<Arguments> cellFilterTags() {
    return Stream.of(
        arguments(
            "@" + JEXL_ROW_FILTER_TAG + "(A.hasTag(\"orgAdmin\"))",
            "A",
            new JexlCellFilter("A", "A.hasTag(\"orgAdmin\")")));
  }

  @ParameterizedTest
  @MethodSource("cellFilterTags")
  void shouldParseCellFilterTags(String tagString, String expectedColumn, CellFilter expectedTag) {
    // act
    ParsedTags tagCollector = tagParser.parseTags(List.of(tagString), columns);
    // assert
    assertThat(tagCollector.getCellFilters())
        .isEqualTo(Map.of(expectedColumn, List.of(expectedTag)));
  }
}

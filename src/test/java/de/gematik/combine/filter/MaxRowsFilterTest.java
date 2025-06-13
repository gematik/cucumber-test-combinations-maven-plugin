/*
 * Copyright (Change Date see Readme), gematik GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * ******
 *
 * For additional notes and disclaimer from gematik and in case of changes by gematik find details in the "Readme" file.
 */

package de.gematik.combine.filter;

import static de.gematik.utils.MockPluginLog.withMockedPluginLog;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import de.gematik.combine.filter.table.MaxRowsFilter;
import de.gematik.combine.model.CombineItem;
import de.gematik.combine.model.TableCell;
import java.util.List;
import java.util.stream.Stream;
import lombok.SneakyThrows;
import org.apache.maven.plugin.MojoExecutionException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class MaxRowsFilterTest {

  private List<List<TableCell>> singleColumn(String... values) {
    return stream(values)
        .map(
            value ->
                List.of(
                    TableCell.builder()
                        .header("A")
                        .combineItem(
                            CombineItem.builder()
                                .value(value)
                                .property("prop", value)
                                .tag(value)
                                .build())
                        .build()))
        .collect(toList());
  }

  static Stream<Arguments> maxRowFilters() {
    return Stream.of(
        arguments("1", 1),
        arguments("4", 4),
        arguments("5", 4),
        arguments("2+1", 3),
        arguments("rowCount-2", 2),
        arguments("columnCount*2", 2),
        arguments("A.tags.count(\"foo\")", 2),
        arguments("A.properties[\"prop\"].distinct().size()", 3));
  }

  @ParameterizedTest
  @MethodSource("maxRowFilters")
  @SneakyThrows
  void testMaxRowFilters(String filterExpression, int expectedSize) {
    // arrange
    MaxRowsFilter filter = new MaxRowsFilter(filterExpression);
    List<List<TableCell>> column = singleColumn("foo", "bar", "foo", "baz");

    // act
    List<List<TableCell>> filtered = withMockedPluginLog(() -> filter.apply(column));

    // assert
    assertThat(filtered).hasSize(expectedSize);
  }

  @Test
  @SneakyThrows
  void shouldRejectInvalidExpression() {
    // arrange
    MaxRowsFilter filter = new MaxRowsFilter("invalid");
    List<List<TableCell>> column = singleColumn("foo");

    // act
    assertThatThrownBy(() -> withMockedPluginLog(() -> filter.apply(column)))
        // assert
        .isInstanceOf(MojoExecutionException.class)
        .hasMessageContaining("Could not evaluate expression 'invalid'");
  }
}

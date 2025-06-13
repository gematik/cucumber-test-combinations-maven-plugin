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

package de.gematik.combine.execution;

import static java.util.stream.Collectors.joining;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.gematik.combine.CombineMojo;
import de.gematik.combine.FilterConfiguration;
import de.gematik.combine.filter.Filters;
import de.gematik.combine.filter.table.cell.CellFilter;
import de.gematik.combine.filter.table.cell.JexlCellFilter;
import de.gematik.combine.filter.table.row.DistinctRowPropertyFilter;
import de.gematik.combine.filter.table.row.EqualRowPropertyFilter;
import de.gematik.combine.filter.table.row.JexlRowFilter;
import de.gematik.combine.filter.table.row.TableRowFilter;
import de.gematik.combine.model.CombineItem;
import de.gematik.combine.model.TableCell;
import de.gematik.combine.tags.ConfiguredFilters;
import java.util.List;
import java.util.Map;
import org.apache.maven.plugin.logging.Log;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TableGeneratorTest {

  Log log;

  @BeforeEach
  public void setup() {
    CombineMojo mojo = mock(CombineMojo.class);
    log = mock(Log.class);
    when(mojo.getLog()).thenReturn(log);
    CombineMojo.setInstance(mojo);
  }

  TableGenerator tableGenerator = new TableGenerator();

  @Nested
  class FullTable {

    List<CombineItem> items =
        List.of(
            CombineItem.builder().value("Api1").build(),
            CombineItem.builder().value("Api2").build(),
            CombineItem.builder().value("Api3").build());
    List<String> headers = List.of("HEADER_1", "HEADER_2");

    @Test
    void shouldCreateCartesianProduct() {
      // arrange
      ConfiguredFilters configuredFilters =
          new ConfiguredFilters(
              FilterConfiguration.builder().build(), headers, Filters.builder().build());
      // act
      List<List<TableCell>> table = tableGenerator.generateTable(items, configuredFilters);
      // assert
      assertThat(table)
          .extracting(row -> row.stream().map(TableCell::getValue).collect(joining(",")))
          .containsExactly(
              "Api1,Api1",
              "Api1,Api2",
              "Api1,Api3",
              "Api2,Api1",
              "Api2,Api2",
              "Api2,Api3",
              "Api3,Api1",
              "Api3,Api2",
              "Api3,Api3");
    }

    @Test
    void shouldEvaluatePrepareColumnFilter() {
      // arrange
      Map<String, List<CellFilter>> preparedColumnFilters =
          preparedColumnFilters("HEADER_1", "HEADER_1.value.equals(\"Api1\")");
      ConfiguredFilters configuredFilters =
          new ConfiguredFilters(
              FilterConfiguration.builder().build(),
              headers,
              Filters.builder().cellFilters(preparedColumnFilters).build());
      // act
      List<List<TableCell>> table = tableGenerator.generateTable(items, configuredFilters);
      // assert
      assertThat(table)
          .extracting(row -> row.stream().map(TableCell::getValue).collect(joining(",")))
          .containsExactly("Api1,Api1", "Api1,Api2", "Api1,Api3");
    }
  }

  @Nested
  class MinimalTable {

    FilterConfiguration config = FilterConfiguration.builder().minimalTable(true).build();
    List<String> headers = List.of("HEADER_1", "HEADER_2", "HEADER_3");
    List<CombineItem> items =
        List.of(
            CombineItem.builder().value("Api_A1").property("prop", "A").build(),
            CombineItem.builder().value("Api_A2").property("prop", "A").build(),
            CombineItem.builder().value("Api_B1").property("prop", "B").build(),
            CombineItem.builder().value("Api_B2").property("prop", "B").build(),
            CombineItem.builder().value("Api_C1").property("prop", "C").build());

    @Test
    void shouldEvaluateFilterAccessingNotAdjacentColumns() {
      // arrange
      ConfiguredFilters configuredFilters =
          new ConfiguredFilters(
              config,
              headers,
              Filters.builder()
                  .tableRowFilters(
                      List.of(
                          new JexlRowFilter(
                              "HEADER_1.properties[\"prop\"].equals(HEADER_3.properties[\"prop\"])")))
                  .build());
      // act
      List<List<TableCell>> table = tableGenerator.generateTable(items, configuredFilters);
      // assert
      assertThat(table)
          .extracting(row -> row.stream().map(TableCell::getValue).collect(joining(",")))
          .containsExactly("Api_A1,Api_A2,Api_A1", "Api_B1,Api_B2,Api_B1", "Api_C1,Api_A1,Api_C1");
    }

    @Test
    void shouldCreateMinimalTable() {
      // arrange
      ConfiguredFilters configuredFilters =
          new ConfiguredFilters(config, headers, Filters.builder().build());
      // act
      List<List<TableCell>> table = tableGenerator.generateTable(items, configuredFilters);
      // assert
      assertThat(table)
          .extracting(row -> row.stream().map(TableCell::getValue).collect(joining(",")))
          .containsExactly("Api_A1,Api_A2,Api_B1", "Api_B2,Api_C1,Api_A1");
    }

    @Test
    void shouldEvaluatePrepareColumnFilter() {
      // arrange
      Map<String, List<CellFilter>> preparedColumnFilters =
          preparedColumnFilters("HEADER_1", "HEADER_1.value.equals(\"Api_A1\")");

      ConfiguredFilters configuredFilters =
          new ConfiguredFilters(
              config, headers, Filters.builder().cellFilters(preparedColumnFilters).build());
      // act
      List<List<TableCell>> table = tableGenerator.generateTable(items, configuredFilters);
      // assert
      assertThat(table)
          .extracting(row -> row.stream().map(TableCell::getValue).collect(joining(",")))
          .containsExactly("Api_A1,Api_A2,Api_B1", "Api_A1,Api_B2,Api_C1");
    }

    @Test
    void shouldEvaluateRowFilter() {
      // arrange
      List<TableRowFilter> rowFilters = List.of(new DistinctRowPropertyFilter("prop"));
      ConfiguredFilters configuredFilters =
          new ConfiguredFilters(
              config, headers, Filters.builder().tableRowFilters(rowFilters).build());
      // act
      List<List<TableCell>> table = tableGenerator.generateTable(items, configuredFilters);
      // assert
      assertThat(table)
          .extracting(row -> row.stream().map(TableCell::getValue).collect(joining(",")))
          .containsExactly("Api_A1,Api_B1,Api_C1", "Api_A2,Api_B2,Api_C1");
    }

    @Test
    void shouldEvaluatePrepareColumnAndRowFilter() {
      // arrange
      Map<String, List<CellFilter>> preparedColumnFilters =
          preparedColumnFilters("HEADER_2", "HEADER_2.value.equals(\"Api_C1\")");
      List<TableRowFilter> rowFilters = List.of(new DistinctRowPropertyFilter("prop"));

      ConfiguredFilters configuredFilters =
          new ConfiguredFilters(
              config,
              headers,
              Filters.builder()
                  .cellFilters(preparedColumnFilters)
                  .tableRowFilters(rowFilters)
                  .build());
      // act
      List<List<TableCell>> table = tableGenerator.generateTable(items, configuredFilters);
      // assert
      assertThat(table)
          .extracting(row -> row.stream().map(TableCell::getValue).collect(joining(",")))
          .containsExactly("Api_A1,Api_C1,Api_B1", "Api_A2,Api_C1,Api_B2");
    }

    @Test
    void shouldEvaluatePrepareColumnAndRowFilter2() {
      // arrange
      List<CombineItem> items =
          List.of(
              CombineItem.builder().value("Api_A1").property("prop", "A").build(),
              CombineItem.builder().value("Api_A2").tag("orgAdmin").property("prop", "A").build(),
              CombineItem.builder().value("Api_B1").property("prop", "B").build(),
              CombineItem.builder().value("Api_B2").tag("orgAdmin").property("prop", "B").build(),
              CombineItem.builder().value("Api_C1").property("prop", "C").build());
      Map<String, List<CellFilter>> preparedColumnFilters =
          preparedColumnFilters("HEADER_1", "HEADER_1.hasTag(\"orgAdmin\")");
      List<TableRowFilter> rowFilters = List.of(new EqualRowPropertyFilter("prop"));

      ConfiguredFilters configuredFilters =
          new ConfiguredFilters(
              config,
              headers,
              Filters.builder()
                  .cellFilters(preparedColumnFilters)
                  .tableRowFilters(rowFilters)
                  .build());
      // act
      List<List<TableCell>> table = tableGenerator.generateTable(items, configuredFilters);
      // assert
      assertThat(table)
          .extracting(row -> row.stream().map(TableCell::getValue).collect(joining(",")))
          .containsExactly("Api_A2,Api_A1,Api_A1", "Api_B2,Api_B1,Api_B1");
    }
  }

  private Map<String, List<CellFilter>> preparedColumnFilters(
      String column, String filterExpression) {
    return Map.of(column, List.of(new JexlCellFilter(column, filterExpression)));
  }
}

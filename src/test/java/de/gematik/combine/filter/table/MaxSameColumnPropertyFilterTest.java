/*
 * Copyright (c) 2022 gematik GmbH
 * 
 * Licensed under the Apache License, Version 2.0 (the License);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.gematik.combine.filter.table;

import static de.gematik.combine.MockPluginLog.withMockedPluginLog;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

import de.gematik.combine.model.CombineItem;
import de.gematik.combine.model.TableCell;
import java.util.List;
import java.util.stream.Stream;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

class MaxSameColumnPropertyFilterTest {

  public static final String COLUMN_NAME = "A";
  public static final String PROPERTY_NAME = "prop";

  private List<List<TableCell>> table() {
    return Stream.of(
            combineItem("A", PROPERTY_NAME, "value"),
            combineItem("B", "otherProp", "value"),
            combineItem("C", PROPERTY_NAME, "other"),
            combineItem("D", PROPERTY_NAME, "value"),
            combineItem("E", PROPERTY_NAME, "value"),
            combineItem("F", PROPERTY_NAME, "other")
        )
        .map(ci -> new TableCell(COLUMN_NAME, ci))
        .map(List::of)
        .collect(toList());
  }

  private CombineItem combineItem(String value, String propertyName, String propValue) {
    return CombineItem.builder()
        .value(value)
        .property(propertyName, propValue)
        .build();
  }

  @Test
  @SneakyThrows
  void shouldRemoveSecond() {
    // arrange
    MaxSameColumnPropertyFilter filter = new MaxSameColumnPropertyFilter(COLUMN_NAME, PROPERTY_NAME, 1);
    // act
    List<List<TableCell>> result = withMockedPluginLog(() -> filter.apply(table()));

    // assert
    assertThat(result)
        .flatExtracting(row -> row.stream()
            .map(TableCell::getValue)
            .collect(toList()))
        .containsExactly("A", "B", "C");
  }

  @Test
  @SneakyThrows
  void shouldRemoveThird() {
    // arrange
    MaxSameColumnPropertyFilter filter = new MaxSameColumnPropertyFilter(COLUMN_NAME, PROPERTY_NAME, 2);
    // act
    List<List<TableCell>> result = withMockedPluginLog(() -> filter.apply(table()));

    // assert
    assertThat(result)
        .flatExtracting(row -> row.stream()
            .map(TableCell::getValue)
            .collect(toList()))
        .containsExactly("A", "B", "C", "D", "F");
  }

}

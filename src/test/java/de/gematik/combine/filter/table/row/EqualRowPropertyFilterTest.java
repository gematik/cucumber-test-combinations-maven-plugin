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

package de.gematik.combine.filter.table.row;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import de.gematik.combine.model.CombineItem;
import de.gematik.combine.model.TableCell;
import java.util.List;
import java.util.stream.Stream;
import lombok.SneakyThrows;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class EqualRowPropertyFilterTest {

  public static final String PROPERTY_NAME = "homeserver";

  private static List<TableCell> rowValues(String... propertyValues) {
    return stream(propertyValues)
        .map(
            prop ->
                TableCell.builder()
                    .combineItem(
                        CombineItem.builder()
                            .property(PROPERTY_NAME, prop)
                            .value(prop + "value")
                            .build())
                    .build())
        .collect(toList());
  }

  public static Stream<Arguments> testParameter() {
    return Stream.of(
        arguments(true, rowValues("foo")),
        arguments(false, rowValues("foo", "bar")),
        arguments(false, rowValues("foo", "bar", "baz")),
        arguments(true, rowValues("foo", "foo")),
        arguments(false, rowValues("foo", "bar", "foo", "bar")));
  }

  @ParameterizedTest
  @MethodSource("testParameter")
  @SneakyThrows
  void shouldFilter(boolean expectedFiltered, List<TableCell> row) {
    // arrange
    EqualRowPropertyFilter filter = new EqualRowPropertyFilter(PROPERTY_NAME);
    // act
    boolean filtered = filter.test(row);
    // assert
    assertThat(filtered).isEqualTo(expectedFiltered);
  }
}

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

package de.gematik.combine.filter.table.row;

import static de.gematik.combine.filter.table.row.SelfCombineFilterTest.State.ALLOW_SELF_COMBINE;
import static de.gematik.combine.filter.table.row.SelfCombineFilterTest.State.DISALLOW_SELF_COMBINE;
import static de.gematik.combine.filter.table.row.SelfCombineFilterTest.TestResult.KEEP;
import static de.gematik.combine.filter.table.row.SelfCombineFilterTest.TestResult.REMOVE;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import de.gematik.combine.model.CombineItem;
import de.gematik.combine.model.TableCell;
import java.util.List;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class SelfCombineFilterTest {


  @RequiredArgsConstructor
  enum State {
    ALLOW_SELF_COMBINE(true),
    DISALLOW_SELF_COMBINE(false);
    final boolean value;
  }

  @RequiredArgsConstructor
  enum TestResult {
    KEEP(true),
    REMOVE(false);
    final boolean value;
  }

  private static List<TableCell> rowValues(String... values) {
    return stream(values)
        .map(value ->
            TableCell.builder()
                .combineItem(CombineItem.builder()
                    .value(value)
                    .build())
                .build())
        .collect(toList());
  }

  public static Stream<Arguments> testParameter() {
    return Stream.of(
        arguments(KEEP, DISALLOW_SELF_COMBINE, rowValues("foo")),
        arguments(KEEP, DISALLOW_SELF_COMBINE, rowValues("foo", "bar")),
        arguments(KEEP, DISALLOW_SELF_COMBINE, rowValues("foo", "bar", "baz")),
        arguments(REMOVE, DISALLOW_SELF_COMBINE, rowValues("foo", "bar", "foo")),
        arguments(KEEP, ALLOW_SELF_COMBINE, rowValues("foo", "foo")),
        arguments(REMOVE, DISALLOW_SELF_COMBINE, rowValues("foo", "bar", "foo", "bar"))
    );
  }

  @ParameterizedTest
  @MethodSource("testParameter")
  @SneakyThrows
  void shouldFilter(TestResult testResult, State state, List<TableCell> row) {
    // arrange
    SelfCombineFilter distinctRowPropertyTag = new SelfCombineFilter(state.value);
    // act
    boolean filtered = distinctRowPropertyTag.test(row);
    // assert
    assertThat(filtered).isEqualTo(testResult.value);
  }

}

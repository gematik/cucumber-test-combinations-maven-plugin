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

package de.gematik.combine.filter;

import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.stream.Stream;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

class FilterOrderTest {

  @Test
  @SneakyThrows
  void enumShouldBeOrdered() {
    // arrange
    List<FilterOrder> sortedByOrdinal =
        Stream.of(FilterOrder.values()).sorted(comparingInt(Enum::ordinal)).collect(toList());

    // act
    List<FilterOrder> sortedByOrderKey =
        Stream.of(FilterOrder.values())
            .sorted(comparingInt(FilterOrder::getOrderKey))
            .collect(toList());

    // assert
    assertThat(sortedByOrderKey)
        .as("Enum FilterOrder is not ordered by orderKey")
        .isEqualTo(sortedByOrdinal);
  }

  @Test
  @SneakyThrows
  void shouldRejectUnknownClass() {
    // act
    assertThatThrownBy(() -> FilterOrder.getFilterOrderFor(lists -> lists))
        // assert
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("has no FilterOrder entry");
  }
}

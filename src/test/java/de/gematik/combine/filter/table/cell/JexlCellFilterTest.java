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

package de.gematik.combine.filter.table.cell;

import static org.assertj.core.api.Assertions.assertThat;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

class JexlCellFilterTest {

  @Test
  @SneakyThrows
  void shouldCombine() {
    // arrange
    JexlCellFilter filter = new JexlCellFilter("expression1", "header1");
    JexlCellFilter filter2 = new JexlCellFilter("expression2", "header2");
    // act
    CellFilter combined = filter.and(filter2);

    // assert
    assertThat(combined).isNotNull();
  }
}

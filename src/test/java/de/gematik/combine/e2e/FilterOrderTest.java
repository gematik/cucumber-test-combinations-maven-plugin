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

package de.gematik.combine.e2e;

import static org.assertj.core.api.Assertions.assertThat;

import de.gematik.combine.FilterConfiguration;
import org.junit.jupiter.api.Test;

class FilterOrderTest extends AbstractCombineMojoTest {

  public static final String FILTER_ORDER_FILE = "filterOrder";

  @Test
  void shouldApplyFiltersInOrder() {
    // arrange
    combineMojo.setFilterConfiguration(
        FilterConfiguration.builder().allowSelfCombine(true).allowDoubleLineup(true).build());
    // act
    combineMojo.execute();
    // assert
    String strippedStr = readFile(FILTER_ORDER_FILE);
    assertThat(strippedStr).endsWith("|HEADER_1|HEADER_2|\n" + "|Api1|Api2|\n" + "|Api4|Api2|\n");
  }
}

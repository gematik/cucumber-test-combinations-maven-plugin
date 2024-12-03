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

package de.gematik.combine.e2e;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.quality.Strictness.LENIENT;

import de.gematik.combine.FilterConfiguration;
import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;

@MockitoSettings(strictness = LENIENT)
class FilterPreparedColumnsTest extends AbstractCombineMojoTest {

  public static final String FILTER_ORDER_FILE = "preparedFilters";

  @Test
  void shouldNotExperienceOutOfMemoryError() {
    // arrange
    combineMojo.setFilterConfiguration(
        FilterConfiguration.builder().allowSelfCombine(true).build());
    // act
    assertThatNoException().isThrownBy(() -> combineMojo.execute());
    // assert
    String strippedStr = readFile(FILTER_ORDER_FILE);
    assertThat(strippedStr).endsWith("|Api8|Api8|Api8|Api8|Api8|Api8|Api8|Api8|Api8|Api8|\n");
  }

  @Override
  protected String combineItemsFile() {
    return "./src/test/resources/input/input5.json";
  }
}

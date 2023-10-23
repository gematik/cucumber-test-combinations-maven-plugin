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

import de.gematik.combine.FilterConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ShuffleTest extends AbstractCombineMojoTest {

  public static final String SHUFFLE_FILE = "shuffle";

  @Test
  void shouldShuffle() {
    // arrange
    combineMojo.setFilterConfiguration(FilterConfiguration.builder()
        .allowDoubleLineup(true)
        .build());
    // act
    combineMojo.execute();
    // assert
    String strippedStr = readFile(SHUFFLE_FILE);
    assertThat(strippedStr)
        .contains("|Api1|Api2|\n",
            "|Api1|Api3|\n",
            "|Api1|Api4|\n",
            "|Api2|Api1|\n",
            "|Api2|Api3|\n",
            "|Api2|Api4|\n",
            "|Api3|Api1|\n",
            "|Api3|Api2|\n",
            "|Api3|Api4|\n",
            "|Api4|Api1|\n",
            "|Api4|Api2|\n",
            "|Api4|Api3|\n")
        .doesNotContain("|Api1|Api2|\n"
            + "|Api1|Api3|\n"
            + "|Api1|Api4|\n"
            + "|Api2|Api1|\n"
            + "|Api2|Api3|\n"
            + "|Api2|Api4|\n"
            + "|Api3|Api1|\n"
            + "|Api3|Api2|\n"
            + "|Api3|Api4|\n"
            + "|Api4|Api1|\n"
            + "|Api4|Api2|\n"
            + "|Api4|Api3|\n");

  }

  @Override
  protected String combineItemsFile() {
    return "./src/test/resources/input/input3.json";
  }

}

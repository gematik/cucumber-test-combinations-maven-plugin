/*
 * Copyright 20023 gematik GmbH
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
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OverrideConfigurationTest extends AbstractCombineMojoTest {

  public static final String CONFIG_TAGS_FILE = "configTags";
  public static final String CONFIG_TAGS_FILE_ENDING = ".configTags";

  @BeforeEach
  public void setup() {
    combineMojo.setEnding(CONFIG_TAGS_FILE_ENDING);
    combineMojo.setFilterConfiguration(FilterConfiguration.builder()
        .allowSelfCombine(true)
        .allowDoubleLineup(true)
        .build());
  }

  @Test
  @SneakyThrows
  void overrideAllowDoubleLineup() {
    // act
    combineMojo.execute();
    // assert
    String strippedStr = readFile(CONFIG_TAGS_FILE);
    assertThat(strippedStr).contains(
        "@Plugin:AllowDoubleLineup(false)\n"
            + "Beispiele:\n"
            + "|HEADER_1|HEADER_2|\n"
            + "|Api1|Api1|\n"
            + "|Api1|Api2|\n"
            + "|Api2|Api2|");
  }

  @Test
  @SneakyThrows
  void overrideAllowSelfCombine() {
    // act
    combineMojo.execute();
    // assert
    String strippedStr = readFile(CONFIG_TAGS_FILE);
    assertThat(strippedStr).contains(
        "@Plugin:AllowSelfCombine(false)\n"
            + "Beispiele:\n"
            + "|HEADER_1|HEADER_2|\n"
            + "|Api1|Api2|\n"
            + "|Api2|Api1|");
  }

  @Test
  @SneakyThrows
  void overrideDoubleLineupAndSelfCombine() {
    // act
    combineMojo.execute();
    // assert
    String strippedStr = readFile(CONFIG_TAGS_FILE);
    assertThat(strippedStr).contains(
        "@Plugin:AllowDoubleLineup(false)@Plugin:AllowSelfCombine(false)\n"
            + "Beispiele:\n"
            + "|HEADER_1|HEADER_2|\n"
            + "|Api1|Api2|");
  }

  @Test
  @SneakyThrows
  void maxRows() {
    // act
    combineMojo.execute();
    // assert
    String strippedStr = readFile(CONFIG_TAGS_FILE);
    assertThat(strippedStr).contains(
        "@Plugin:MaxRows(1)\n"
            + "Beispiele:\n"
            + "|HEADER_1|HEADER_2|\n"
            + "|Api1|Api1|");
  }

  @Override
  protected String combineItemsFile() {
    return "./src/test/resources/input/input4.json";
  }
}

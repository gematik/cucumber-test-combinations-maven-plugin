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

import static java.lang.Integer.MAX_VALUE;
import static org.assertj.core.api.Assertions.assertThat;

import de.gematik.combine.FilterConfiguration;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DefaultConfigurationTest extends AbstractCombineMojoTest {

  public static final String DEFAULT_CONFIG_FILE = "withoutFilters";

  public static final FilterConfiguration defaultConfig = FilterConfiguration.builder()
      .allowSelfCombine(true)
      .allowDoubleLineup(true)
      .maxTableRows(MAX_VALUE)
      .build();

  @BeforeEach
  public void setup() {

    combineMojo.setEnding(".withoutFilters");
    combineMojo.setFilterConfiguration(defaultConfig);
  }

  @Test
  @SneakyThrows
  void allowAll() {
    // act
    combineMojo.execute();
    // assert

    String strippedStr = readFile(DEFAULT_CONFIG_FILE);
    assertThat(strippedStr).endsWith(
        "|HEADER_1|HEADER_2|\n"
            + "|Api1|Api1|\n"
            + "|Api1|Api2|\n"
            + "|Api2|Api1|\n"
            + "|Api2|Api2|\n");
  }

  @Test
  @SneakyThrows
  void onlyAllowDoubleLineup() {
    // arrange
    combineMojo.setFilterConfiguration(defaultConfig.toBuilder()
        .allowSelfCombine(false)
        .allowDoubleLineup(true)
        .build());
    // act
    combineMojo.execute();
    // assert
    String strippedStr = readFile(DEFAULT_CONFIG_FILE);
    assertThat(strippedStr).endsWith(
        "|HEADER_1|HEADER_2|\n"
            + "|Api1|Api2|\n"
            + "|Api2|Api1|\n");
  }

  @Test
  @SneakyThrows
  void onlyAllowSelfCombine() {
    // arrange
    combineMojo.setFilterConfiguration(defaultConfig.toBuilder()
        .allowSelfCombine(true)
        .allowDoubleLineup(false)
        .build());
    // act
    combineMojo.execute();
    // assert
    String strippedStr = readFile(DEFAULT_CONFIG_FILE);
    assertThat(strippedStr).endsWith(
        "|HEADER_1|HEADER_2|\n"
            + "|Api1|Api1|\n"
            + "|Api1|Api2|\n"
            + "|Api2|Api2|\n");
  }

  @Test
  @SneakyThrows
  void denyAll() {
    // arrange
    combineMojo.setFilterConfiguration(defaultConfig.toBuilder()
        .allowSelfCombine(false)
        .allowDoubleLineup(false)
        .build());
    // act
    combineMojo.execute();
    // assert
    String strippedStr = readFile(DEFAULT_CONFIG_FILE);
    assertThat(strippedStr).endsWith(
        "|HEADER_1|HEADER_2|\n"
            + "|Api1|Api2|\n");
  }

  @Test
  @SneakyThrows
  void setMaxRows() {
    // arrange
    combineMojo.setFilterConfiguration(defaultConfig.toBuilder()
        .allowSelfCombine(true)
        .maxTableRows(1)
        .build());
    // act
    combineMojo.execute();
    // assert

    String strippedStr = readFile(DEFAULT_CONFIG_FILE);
    assertThat(strippedStr).endsWith(
        "|HEADER_1|HEADER_2|\n"
            + "|Api1|Api1|\n");
  }

  @Test
  @SneakyThrows
  void shouldSetMaxRow() {
    // arrange
    combineMojo.setFilterConfiguration(defaultConfig.toBuilder()
        .allowSelfCombine(true)
        .maxTableRows(2)
        .build());
    // act
    combineMojo.execute();
    // assert

    String strippedStr = readFile(DEFAULT_CONFIG_FILE);
    assertThat(strippedStr).endsWith(
        "|HEADER_1|HEADER_2|\n"
            + "|Api1|Api1|\n"
            + "|Api1|Api2|\n");
  }

  @Override
  protected String inputDir() {
    return "./src/test/resources/templates";
  }

  @Override
  protected String combineItemsFile() {
    return "./src/test/resources/input/input4.json";
  }
}

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

import de.gematik.combine.ProjectFilters;
import org.junit.jupiter.api.Test;

public class PluginTagPrefixesTest extends AbstractCombineMojoTest {

  public static final String PLUGIN_TAG_CATEGORY = "Lord";
  public static final String VERSION_FILTER_TAG_CATEGORY = "Voldemort";
  public static final String DEFAULT_CONFIG_FILE = "prefixes";

  @Test
  void shouldSetCorrectPrefixes() {
    // arrange
    combineMojo.setEnding(DEFAULT_CONFIG_FILE);
    combineMojo.setPluginTagCategory(PLUGIN_TAG_CATEGORY);
    combineMojo.setVersionFilterTagCategory(VERSION_FILTER_TAG_CATEGORY);
    combineMojo.setProjectFilters(ProjectFilters.builder().version("--GE--2").build());

    // act
    combineMojo.execute();

    // assert
    String strippedStr = readFile(DEFAULT_CONFIG_FILE);
    assertThat(strippedStr.split(PLUGIN_TAG_CATEGORY)).hasSize(2);
    assertThat(strippedStr.split(VERSION_FILTER_TAG_CATEGORY)).hasSize(4);
  }

}

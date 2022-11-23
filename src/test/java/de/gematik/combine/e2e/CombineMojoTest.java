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

package de.gematik.combine.e2e;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.quality.Strictness.LENIENT;

import de.gematik.combine.FilterConfiguration;
import org.apache.maven.plugin.MojoExecutionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;

@MockitoSettings(strictness = LENIENT)
class CombineMojoTest extends AbstractCombineMojoTest {

  public static final String INVALID_GHERKIN_FILE_ENDING = ".invalidGherkin";

  @BeforeEach
  public void setup() {
    combineMojo.setEnding(WITHOUT_FILTERS_FILE_ENDING);
    combineMojo.setFilterConfiguration(FilterConfiguration.builder()
        .allowSelfCombine(true)
        .allowDoubleLineup(true)
        .build());
  }

  @Test
  void templatePathNotFound() {
    // arrange
    combineMojo.setTemplateDir("NotExisting");
    // act
    assertThatThrownBy(() -> combineMojo.execute())
        // assert
        .isInstanceOf(MojoExecutionException.class)
        .hasMessageContaining("Template directory does not exist: ", "NotExisting");
  }

  @Test
  void combineItemsFileNotFound() {
    // arrange
    combineMojo.setCombineItemsFile("NotExisting");
    // act
    assertThatThrownBy(() -> combineMojo.execute())
        // assert
        .isInstanceOf(MojoExecutionException.class)
        .hasMessageContainingAll("Combine items file not found: ", "NotExisting");
  }

  @Test
  void invalidGherkin() {
    // arrange
    combineMojo.setEnding(INVALID_GHERKIN_FILE_ENDING);
    // act
    assertThatThrownBy(() -> combineMojo.execute())
        // assert
        .isInstanceOf(MojoExecutionException.class)
        .hasMessageContainingAll("Could not parse invalid gherkin", "invalidGherkin");
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

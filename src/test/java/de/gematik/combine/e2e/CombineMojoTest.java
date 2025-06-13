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
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.quality.Strictness.LENIENT;

import de.gematik.combine.CombineMojo;
import de.gematik.combine.FilterConfiguration;
import org.apache.maven.plugin.MojoExecutionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;

import java.util.List;

@MockitoSettings(strictness = LENIENT)
class CombineMojoTest extends AbstractCombineMojoTest {

  public static final String INVALID_GHERKIN_FILE_ENDING = ".invalidGherkin";

  @BeforeEach
  public void setup() {
    combineMojo.setEnding(WITHOUT_FILTERS_FILE_ENDING);
    combineMojo.setFilterConfiguration(
        FilterConfiguration.builder().allowSelfCombine(true).allowDoubleLineup(true).build());
  }

  @Test
  void templatePathNotFound() {
    // arrange
    combineMojo.setTemplateSources(List.of("NotExisting"));
    // act
    assertThatThrownBy(() -> combineMojo.execute())
        // assert
        .isInstanceOf(MojoExecutionException.class)
        .hasMessageContaining("Template directory does not exist: ", "NotExisting");
  }

  @Test
  void findFileWithSameNameTwice() {
    // arrange
    combineMojo.setTemplateSources(List.of(inputDir(), inputDir()));
    // act
    assertThatThrownBy(() -> combineMojo.execute())
        // assert
        .isInstanceOf(MojoExecutionException.class)
        .hasMessageContaining("Template file "," has the same name as ");
  }

  @Test
  void useMultipleTemplateDirs() {
    // arrange
    combineMojo.setEnding(".cute");
    combineMojo.setTemplateSources(List.of(inputDir()+"/SoftFilterTest", inputDir()+"/ShuffleTest"));
    // act
    assertThatNoException().isThrownBy(combineMojo::execute);
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
    combineMojo.execute();
    // assert
    assertThat(CombineMojo.getWarningsLog()).hasSize(1);
    assertThat(CombineMojo.getWarningsLog().get(0)).contains("Could not parse invalid gherkin", "invalidGherkin", "10:0", "unexpected end of file");
  }

  @Test
  void invalidDescriptionBeforeFeature() {
    // arrange
    combineMojo.setEnding(INVALID_GHERKIN_FILE_ENDING+"2");
    // act
    combineMojo.execute();
    // assert
    assertThat(CombineMojo.getWarningsLog()).hasSize(1);
    assertThat(CombineMojo.getWarningsLog().get(0)).contains("Could not parse invalid gherkin", "invalidGherkin", "2:1", "expected: #TagLine, #FeatureLine, #Comment, #Empty", "Beschreibung");
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

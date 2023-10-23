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

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.quality.Strictness.LENIENT;

import java.util.List;
import java.util.stream.Stream;
import lombok.SneakyThrows;
import org.apache.maven.plugin.MojoExecutionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoSettings;

@MockitoSettings(strictness = LENIENT)
public class DefaultTagsTest extends AbstractCombineMojoTest {

  public static final String DEFAULT_CONFIG_FILE = "withoutFilters";

  public static Stream<Arguments> invalidTags() {
    return Stream.of(
        arguments("Huhu"),
        arguments("@@Huhu"),
        arguments("@Hu hu"),
        arguments("@Huhu@Duda")
    );
  }

  public static Stream<Arguments> configTags() {
    return Stream.of(
        arguments("@AllowDoubleLineup"),
        arguments("@AllowSelfCombine"),
        arguments("@MinimalTable")
    );
  }

  @BeforeEach
  public void setup() {
    combineMojo.setEnding(WITHOUT_FILTERS_FILE_ENDING);
    combineMojo.setDefaultExamplesTags(List.of("@MaxRows(1)", "@Blub"));
  }

  @Test
  @SneakyThrows
  void shouldSetDefaultTags() {
    // arrange
    String pluginTagCategory = combineMojo.getPluginTagCategory();

    // act
    combineMojo.execute();

    // assert
    String strippedStr = readFile(DEFAULT_CONFIG_FILE);
    assertThat(strippedStr).endsWith(
        format("@%s:MaxRows(1)@%s:Blub\n"
                + "Beispiele:\n"
                + "|HEADER_1|HEADER_2|\n"
                + "|Api1|Api2|\n",
            pluginTagCategory, pluginTagCategory));
  }

  @ParameterizedTest
  @MethodSource("invalidTags")
  @SneakyThrows
  void shouldRejectDefaultTag(String invalidTag) {
    // arrange
    combineMojo.setDefaultExamplesTags(List.of(invalidTag));
    // act
    assertThatThrownBy(combineMojo::execute)
        // assert
        .isInstanceOf(MojoExecutionException.class)
        .hasMessageContaining(invalidTag + " is not a valid default tag");
  }

  @ParameterizedTest
  @MethodSource("configTags")
  @SneakyThrows
  void shouldRejectConfigDefaultTag(String configTag) {
    // arrange
    combineMojo.setDefaultExamplesTags(List.of(configTag));
    // act
    assertThatThrownBy(combineMojo::execute)
        // assert
        .isInstanceOf(MojoExecutionException.class)
        .hasMessageContaining("Default tag '" + configTag
            + "' is not allowed to contain configuration tags! [AllowDoubleLineup, AllowSelfCombine, MinimalTable]");
  }

  @Override
  protected String outputDir() {
    return "./target/test/defaultTagsTest";
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

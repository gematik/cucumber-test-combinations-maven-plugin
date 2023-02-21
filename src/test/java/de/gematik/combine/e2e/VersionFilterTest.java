/*
 * Copyright (c) 2023 gematik GmbH
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

import static de.gematik.combine.util.CompareOperator.EQ;
import static de.gematik.combine.util.CompareOperator.GE;
import static de.gematik.combine.util.CompareOperator.GT;
import static de.gematik.combine.util.CompareOperator.LE;
import static de.gematik.combine.util.CompareOperator.LT;
import static de.gematik.combine.util.CompareOperator.NE;
import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import de.gematik.combine.CombineMojo;
import de.gematik.combine.FilterConfiguration;
import de.gematik.combine.ProjectFilters;
import de.gematik.combine.util.CompareOperator;
import java.lang.reflect.MalformedParametersException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class VersionFilterTest extends AbstractCombineMojoTest {

  public static final int PROPERTY_REQUESTS_IN_FILE = 3;
  public static final List<String> ITEMS = List.of("Api0", "Api1", "Api2", "Api3", "Api4");
  public static final String VERSION_FILTER_STRING = ">=1.0.11";
  public static final ProjectFilters PROJECT_FILTERS_WITH_VERSION = ProjectFilters.builder()
      .version(VERSION_FILTER_STRING).build();
  public static final String VERSION_FILTER_TAG_CATEGORY = "WHITE_LOTUS";
  public static final String FILE_NAME = "version";

  public static Stream<Arguments> testOperators() {
    return Stream.of(
        arguments(EQ, List.of("Api2"), 3),
        arguments(NE, List.of("Api0", "Api1", "Api3", "Api4"), 4),
        arguments(LT, List.of("Api0", "Api1"), 2),
        arguments(LE, List.of("Api0", "Api1", "Api2"), 5),
        arguments(GT, List.of("Api3", "Api4"), 2),
        arguments(GE, List.of("Api2", "Api3", "Api4"), 5)
    );
  }

  private static String getRandOperatorString(CompareOperator operator) {
    return format("%s1.0.11",
        Math.random() > 0.5d ? format("--%s--", operator.name()) : operator.getLiteral());
  }

  @Test
  void shouldNotLogExistingProperty() {
    // arrange
    combineMojo.setCombineItemsFile("src/test/resources/input/versions/set.json");

    // act
    combineMojo.execute();

    // assert
    assertThat(CombineMojo.getPropertyErrorLog()).isEmpty();
  }

  @Test
  void shouldLogMissingProperty() {
    // arrange
    combineMojo.setCombineItemsFile("src/test/resources/input/versions/not-set.json");

    // act
    combineMojo.execute();

    // assert
    assertThat(CombineMojo.getPropertyErrorLog()).hasSize(PROPERTY_REQUESTS_IN_FILE);

  }

  @ParameterizedTest
  @ValueSource(ints = {1, 2, 3, 4, 5})
  void shouldThrowExceptionOnMalformedVersions(int num) {
    // arrange
    combineMojo.setCombineItemsFile(
        format("src/test/resources/input/versions/malformed/mal%d.json", num));

    // act + assert
    assertThatThrownBy(() -> combineMojo.execute())
        .isInstanceOf(MalformedParametersException.class)
        .hasMessageStartingWith("Version may only contain numbers");
  }

  @Test
  void shouldThrowExceptionOnMissingOperator() {
    // arrange
    combineMojo.setEnding(".NONE");

    // act + assert
    assertThatThrownBy(() -> combineMojo.execute())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageStartingWith("A version filter must contain a comparison operator: ");
  }

  @Test
  void shouldThrowExceptionOnWrongNumberOfArguments() {
    // arrange
    combineMojo.setEnding(".WRONGARGS");

    // act + assert
    assertThatThrownBy(() -> combineMojo.execute())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("does not have any headers");
  }

  @ParameterizedTest
  @MethodSource("testOperators")
  void shouldFilterAccordingToSetVersionFilters(CompareOperator operator,
      List<String> filteredItems) {
    // arrange
    combineMojo.setEnding(format(".%s", operator.name()));
    combineMojo.setCombineItemsFile("src/test/resources/input/versions/set.json");

    // act
    combineMojo.execute();

    // assert
    assertThat(readFile(FILE_NAME))
        .contains(filteredItems)
        .doesNotContain(ITEMS.stream()
            .filter(item -> !filteredItems.contains(item))
            .collect(Collectors.toList())
        );
  }

  @ParameterizedTest
  @MethodSource("testOperators")
  void shouldFilterAccordingToSetPluginVersionFilter(CompareOperator operator,
      List<String> filteredItems) {
    // arrange
    combineMojo.setEnding(".overall");
    combineMojo.setCombineItemsFile("src/test/resources/input/versions/set.json");
    combineMojo.setProjectFilters(ProjectFilters.builder()
        .version(getRandOperatorString(operator))
        .build());

    // act
    combineMojo.execute();

    // assert
    assertThat(readFile(FILE_NAME))
        .contains(filteredItems)
        .doesNotContain(ITEMS.stream()
            .filter(item -> !filteredItems.contains(item))
            .collect(Collectors.toList())
        );
  }

  @Test
  void shouldAddTagAndPrefixForAppliedProjectVersionFilter() {
    // arrange
    combineMojo.setEnding(".overall");
    combineMojo.setCombineItemsFile("src/test/resources/input/versions/set.json");
    combineMojo.setProjectFilters(PROJECT_FILTERS_WITH_VERSION);
    combineMojo.setVersionFilterTagCategory(VERSION_FILTER_TAG_CATEGORY);

    // act
    combineMojo.execute();

    // assert
    assertThat(readFile(FILE_NAME)).containsOnlyOnce(
        format("@%s:Version(ApiName1A%s)", VERSION_FILTER_TAG_CATEGORY, VERSION_FILTER_STRING));
  }

  @Test
  void shouldAddTagPrefixForAppliedVersionFilter() {
    // arrange
    combineMojo.setEnding(".GE");
    combineMojo.setCombineItemsFile("src/test/resources/input/versions/set.json");
    combineMojo.setVersionFilterTagCategory(VERSION_FILTER_TAG_CATEGORY);

    // act
    combineMojo.execute();

    // assert
    assertThat(readFile(FILE_NAME).split(VERSION_FILTER_TAG_CATEGORY)).hasSize(3);
  }

  @ParameterizedTest
  @MethodSource("testOperators")
  void shouldFilterAccordingToOverrideVersionFilter(CompareOperator operator,
      List<String> ignored, Integer i) {
    // arrange
    combineMojo.setEnding(".override");
    combineMojo.setCombineItemsFile("src/test/resources/input/versions/set.json");
    combineMojo.setFilterConfiguration(
        FilterConfiguration.builder().allowSelfCombine(true).build());
    combineMojo.setProjectFilters(ProjectFilters.builder()
        .version(getRandOperatorString(operator)).build());

    // act
    combineMojo.execute();

    // assert
    assertThat(readFile(FILE_NAME).split("Api2")).hasSize(i + 1);
  }
}

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

import static io.cucumber.messages.types.SourceMediaType.TEXT_X_CUCUMBER_GHERKIN_PLAIN;
import static org.assertj.core.api.Assertions.assertThat;

import de.gematik.combine.filter.table.SoftFilter;
import io.cucumber.gherkin.GherkinParser;
import io.cucumber.messages.types.Envelope;
import io.cucumber.messages.types.Examples;
import io.cucumber.messages.types.FeatureChild;
import io.cucumber.messages.types.GherkinDocument;
import io.cucumber.messages.types.Scenario;
import io.cucumber.messages.types.Source;
import io.cucumber.messages.types.TableRow;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class SoftFilterTest extends AbstractCombineMojoTest {

  public static final String REMOVE_TEST_NAME = "ShouldRemoveSoftCellFilter";
  public static final String APPLY_TEST_NAME = "ShouldApplySoftCellFilter";
  public static final String PROPERTY_TEST_NAME = "PropertySoftFilterTest";
  private static final String SOFT_FILTER_FILE = "softFilter";
  private boolean haveToRun = true;

  @BeforeEach
  void init() {
    if (haveToRun) {
      combineMojo.setSoftFilterToHardFilter(false);
      combineMojo.execute();
      haveToRun = false;
    }
  }

  private static Stream<Arguments> softFilterTest() {
    return Stream.of(
        Arguments.of(REMOVE_TEST_NAME, 2),
        Arguments.of(APPLY_TEST_NAME, 1),
        Arguments.of(PROPERTY_TEST_NAME, 5));
  }

  @ParameterizedTest
  @MethodSource
  @SneakyThrows
  void softFilterTest(String scenarioName, int expectedTableSize) {
    assertThat(getTableRows(readFile(SOFT_FILTER_FILE), scenarioName)).hasSize(expectedTableSize);
  }

  @Test
  void testDefaultFromSoftFilter() {
    // arrange
    SoftFilter softFilter = new SoftFilter() {};
    // assert
    assertThat(softFilter.isSoft()).isFalse();
    assertThat(softFilter.setSoft(true)).isEqualTo(softFilter);
  }

  private List<TableRow> getTableRows(String gherkinString, String methodName) {
    return parseGherkinString(gherkinString).getFeature().orElseThrow().getChildren().stream()
        .map(FeatureChild::getScenario)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .filter(s -> s.getName().equals(methodName))
        .map(Scenario::getExamples)
        .flatMap(Collection::stream)
        .map(Examples::getTableBody)
        .flatMap(Collection::stream)
        .collect(Collectors.toList());
  }

  private static GherkinDocument parseGherkinString(String gherkin) {
    final GherkinParser parser =
        GherkinParser.builder()
            .includeSource(false)
            .includePickles(false)
            .includeGherkinDocument(true)
            .build();

    final Source source = new Source("not needed", gherkin, TEXT_X_CUCUMBER_GHERKIN_PLAIN);
    final Envelope envelope = Envelope.of(source);

    return parser
        .parse(envelope)
        .map(Envelope::getGherkinDocument)
        .flatMap(Optional::stream)
        .findAny()
        .orElseThrow(() -> new IllegalArgumentException("Could not parse invalid gherkin."));
  }

  @Override
  protected String combineItemsFile() {
    return "./src/test/resources/input/SoftFilter.json";
  }
}

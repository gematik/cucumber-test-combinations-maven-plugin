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

package de.gematik.combine.execution;

import static de.gematik.combine.CombineMojo.getPluginLog;
import static java.lang.String.format;

import de.gematik.combine.CombineConfiguration;
import de.gematik.combine.count.ExecutionCounter;
import de.gematik.combine.model.CombineItem;
import io.cucumber.messages.types.Feature;
import io.cucumber.messages.types.FeatureChild;
import io.cucumber.messages.types.GherkinDocument;
import io.cucumber.messages.types.Scenario;
import java.util.List;
import java.util.Optional;
import javax.inject.Inject;

import io.cucumber.messages.types.Tag;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(onConstructor_ = @Inject)
public class GherkinProcessor {

  private final ScenarioProcessor scenarioProcessor;

  public int generateExamples(
      GherkinDocument gherkinDocument,
      CombineConfiguration config,
      List<CombineItem> combineItems) {
    if (fileShouldBeSkipped(gherkinDocument, config.getSkipTags())) {
      return 0;
    }

    List<Scenario> scenarios = findMatchingScenarios(gherkinDocument, config);

    getPluginLog().debug(format("processing %d scenarios: ", scenarios.size()));
    scenarios.forEach(scenario -> scenarioProcessor.generateExamples(scenario, config, combineItems));
    return countScenarios(scenarios);
  }

  private static List<Scenario> findMatchingScenarios(GherkinDocument gherkinDocument, CombineConfiguration config) {
    List<Scenario> scenarios = allScenariosInDocument(gherkinDocument, config);
    if (!config.filterTagsMatch(getFeatureTags(gherkinDocument))) {
      scenarios =
          scenarios.stream()
              .filter(scenario -> config.filterTagsMatch(getScenarioTags(scenario)))
              .toList();
    }
    return scenarios;
  }

  private int countScenarios(List<Scenario> scenarios) {
    return scenarios.stream()
        .mapToInt(ExecutionCounter::countScenarios)
        .sum();
  }

  private boolean fileShouldBeSkipped(GherkinDocument gherkinDocument, List<String> skipTags) {
    return gherkinDocument.getFeature().stream()
        .map(Feature::getTags)
        .flatMap(List::stream)
        .map(Tag::getName)
        .map(String::toLowerCase)
        .anyMatch(skipTags::contains);
  }

  private static List<Scenario> allScenariosInDocument(
      GherkinDocument document, CombineConfiguration config) {
    return document.getFeature().stream()
        .map(Feature::getChildren)
        .flatMap(List::stream)
        .map(FeatureChild::getScenario)
        .flatMap(Optional::stream)
        .filter(scenario -> scenarioWithoutSkipTag(scenario, config.getSkipTags()))
        .toList();
  }

  private static boolean scenarioWithoutSkipTag(Scenario scenario, List<String> skipTags) {
    return scenario.getTags().stream()
        .map(Tag::getName)
        .map(String::toLowerCase)
        .noneMatch(skipTags::contains);
  }

  private static List<String> getFeatureTags(GherkinDocument gherkinDocument) {
    return gherkinDocument.getFeature().stream()
        .map(Feature::getTags)
        .flatMap(List::stream)
        .map(Tag::getName)
        .toList();
  }

  private static List<String> getScenarioTags(Scenario scenario) {
    return scenario.getTags().stream().map(Tag::getName).toList();
  }
}

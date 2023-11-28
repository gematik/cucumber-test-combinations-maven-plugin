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

package de.gematik.combine.execution;

import de.gematik.combine.CombineConfiguration;
import de.gematik.combine.model.CombineItem;
import io.cucumber.messages.types.GherkinDocument;
import io.cucumber.messages.types.Scenario;
import lombok.RequiredArgsConstructor;

import javax.inject.Inject;
import java.util.List;

import static de.gematik.combine.CombineMojo.getPluginLog;
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

@RequiredArgsConstructor(onConstructor_ = @Inject)
public class GherkinProcessor {

  private final ScenarioProcessor scenarioProcessor;

  public void process(GherkinDocument gherkinDocument,
                      CombineConfiguration config, List<CombineItem> combineItems) {
    if (fileShouldBeSkipped(gherkinDocument, config.getSkipTags())) {
      return;
    }
    List<Scenario> scenarios = allScenariosInDocument(gherkinDocument, config);

    getPluginLog().debug(format("processing %d scenarios: ", scenarios.size()));
    scenarios.forEach(
            scenario -> scenarioProcessor.process(scenario, config, combineItems));
  }

  private boolean fileShouldBeSkipped(GherkinDocument gherkinDocument, List<String> skipTags) {
    if (gherkinDocument.getFeature().isPresent()) {
      return !gherkinDocument.getFeature().orElseThrow().getTags().stream()
              .map(tag -> tag.getName().toLowerCase())
              .noneMatch(skipTags::contains);
    }
    return false;
  }

  private static List<Scenario> allScenariosInDocument(GherkinDocument document,
                                                       CombineConfiguration config) {
    return document.getFeature().stream()
            .flatMap(feature -> feature.getChildren().stream())
            .flatMap(child -> child.getScenario().stream())
            .filter(scenario -> scenarioWithoutSkipTag(scenario, config.getSkipTags()))
            .collect(toList());
  }

  private static boolean scenarioWithoutSkipTag(Scenario scenario, List<String> skipTags) {
    return scenario.getTags().stream()
            .map(tag -> tag.getName().toLowerCase())
            .noneMatch(skipTags::contains);
  }

}

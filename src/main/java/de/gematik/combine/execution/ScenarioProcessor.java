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

package de.gematik.combine.execution;

import static de.gematik.combine.CombineMojo.ErrorType.SIZE;
import static de.gematik.combine.CombineMojo.appendError;
import static de.gematik.combine.CombineMojo.getPluginLog;
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

import de.gematik.combine.CombineConfiguration;
import de.gematik.combine.model.CombineItem;
import de.gematik.combine.util.CurrentScenario;
import io.cucumber.messages.types.Examples;
import io.cucumber.messages.types.Location;
import io.cucumber.messages.types.Scenario;
import io.cucumber.messages.types.Tag;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@RequiredArgsConstructor(onConstructor_ = @Inject)
public class ScenarioProcessor {

  public static final Location LOCATION = new Location(0L, 0L);

  private final ExamplesProcessor examplesProcessor;

  public void process(Scenario scenario, CombineConfiguration config,
      List<CombineItem> combineItems) {
    CurrentScenario.setCurrentScenarioName(scenario.getName());

    List<Examples> examples = scenario.getExamples().stream()
        .filter(example -> examplesWithoutSkipTag(example, config.getSkipTags()))
        .collect(toList());

    getPluginLog().debug(format("processing %d examples: ", examples.size()));
    examples.forEach(table -> processExamplesTable(table, combineItems, config, scenario.getName()));

    addEmptyExamplesTags(scenario, config.getEmptyExamplesTags());
    checkTableSize(scenario, config.getMinTableSize());
  }

  private void processExamplesTable(Examples examples, List<CombineItem> combineItems,
      CombineConfiguration configuration, String scenarioName) {
    getPluginLog().debug("processing single examples table" + examples.getName());

    examplesProcessor
        .process(examples, configuration, combineItems, scenarioName);
  }

  private void addEmptyExamplesTags(Scenario scenario, List<String> tags) {
    boolean allExamplesEmpty = scenario.getExamples().stream()
        .allMatch(example -> example.getTableBody().isEmpty());
    if (allExamplesEmpty) {
      addTagsToScenario(scenario, tags);
    }
  }

  @SneakyThrows
  @SuppressWarnings("java:S3011")
  private void addTagsToScenario(Scenario scenario, List<String> additionalTags) {
    List<Tag> tags = new ArrayList<>(scenario.getTags());
    List<Tag> mappedAdditionalTags = additionalTags.stream()
        .map(tag -> new Tag(LOCATION, tag, ""))
        .collect(toList());

    tags.addAll(mappedAdditionalTags);

    Field field = Scenario.class.getDeclaredField("tags");
    field.setAccessible(true);
    field.set(scenario, tags);
  }

  private static boolean examplesWithoutSkipTag(Examples examples, List<String> skipTags) {
    return examples.getTags().stream()
        .map(tag -> tag.getName().toLowerCase())
        .noneMatch(skipTags::contains);
  }

  private void checkTableSize(Scenario scenario, int minTableSize) {
    List<Integer> tableSizes = scenario.getExamples().stream()
        .map(e -> e.getTableBody().size())
        .filter(e -> e < minTableSize)
        .collect(toList());
    if (tableSizes.isEmpty()) {
      return;
    }
    appendError(format(
            "The table of scenario \"%s\" has a table with size %s which is less than the minimal size of %s",
            scenario.getName(), tableSizes.stream().min(Integer::compareTo).orElse(-1), minTableSize),
        SIZE);
  }

}

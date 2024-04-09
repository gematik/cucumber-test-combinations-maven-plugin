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

package de.gematik.combine.count;

import static de.gematik.BaseMojo.GENERATED_COMBINE_ITEMS_DIR;
import static de.gematik.combine.execution.FileProcessor.parseGherkinString;
import static java.util.Objects.requireNonNull;

import de.gematik.combine.CombineConfiguration;
import io.cucumber.core.internal.com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.messages.types.Feature;
import io.cucumber.messages.types.FeatureChild;
import io.cucumber.messages.types.GherkinDocument;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.MojoExecutionException;

public class ExecutionCounter {

  public static final String COUNT_EXECUTION_FILE_NAME = "countExecution";
  private final Set<ExampleCounter> exampleCounter = new TreeSet<>();

  public void count(CombineConfiguration config) {
    if (!config.isCountExecutions()) {
      return;
    }
    List<File> featureFiles = getFeatureFiles(new File(config.getOutputDir()));
    List<GherkinDocument> features = featureFiles.stream().map(this::transformToGherkin).toList();
    countExecutions(features);
    writeExecutionsToFile(config);
  }

  private static List<File> getFeatureFiles(File file) {
    List<File> files = new ArrayList<>();
    for (File f : requireNonNull(file.listFiles())) {
      if (f.isDirectory()) {
        files.addAll(getFeatureFiles(f));
      } else {
        files.add(f);
      }
    }
    return files;
  }

  @SneakyThrows
  private GherkinDocument transformToGherkin(File f) {
    return parseGherkinString(Files.readString(f.toPath()));
  }

  private void countExecutions(List<GherkinDocument> features) {
    features.stream()
        .map(GherkinDocument::getFeature)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .forEach(this::countExamples);
  }

  @SneakyThrows
  private void countExamples(Feature feature) {
    ExampleCounter counter = new ExampleCounter(feature.getName());
    if (exampleCounter.contains(counter)) {
      throw new MojoExecutionException(
          "Could not count features correctly because 2 feature files named the same: "
              + feature.getName());
    }
    exampleCounter.add(counter);
    for (FeatureChild c : feature.getChildren()) {
      c.getScenario()
          .ifPresent(
              s ->
                  s.getExamples()
                      .forEach(
                          e ->
                              counter.addScenario(
                                  c.getScenario().get().getName(), e.getTableBody().size())));
    }
  }

  private void writeExecutionsToFile(CombineConfiguration config) {
    TotalCounter totalCounter = new TotalCounter(exampleCounter);
    if (config.getCountExecutionsFormat().contains("json")) {
      createJsonFile(totalCounter);
    }
    if (config.getCountExecutionsFormat().contains("txt")) {
      createTxtFile(totalCounter);
    }
  }

  @SneakyThrows
  private void createJsonFile(TotalCounter totalCounter) {
    writeLineToStatisticFile(
        new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(totalCounter),
        ".json",
        true);
  }

  private void createTxtFile(TotalCounter totalCounter) {
    writeLineToStatisticFile("total -> " + totalCounter.getTotal(), ".txt", true);
    writeLineToStatisticFile(
        "totalScenarios -> " + totalCounter.getTotalScenarioAmount(), ".txt", false);
    exampleCounter.stream()
        .forEach(
            e -> {
              writeLineToStatisticFile(
                  "\n"
                      + e.getName()
                      + " -> "
                      + e.getScenarios().values().stream().reduce(Integer::sum).orElse(0)
                      + " SzenarioAmount => "
                      + e.getScenarioAmount(),
                  ".txt");
              e.getScenarios()
                  .forEach((k, v) -> writeLineToStatisticFile("\t" + k + " -> " + v, ".txt"));
            });
  }

  private void writeLineToStatisticFile(String s, String fileEnding) {
    writeLineToStatisticFile(s, fileEnding, false);
  }

  @SneakyThrows
  private static void writeLineToStatisticFile(String s, String fileEnding, boolean override) {
    FileUtils.write(
        new File(
            GENERATED_COMBINE_ITEMS_DIR + File.separator + COUNT_EXECUTION_FILE_NAME + fileEnding),
        s + "\n",
        StandardCharsets.UTF_8,
        !override);
  }
}

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

package de.gematik.combine.count;

import static de.gematik.combine.execution.FileProcessor.parseGherkinString;
import static de.gematik.utils.Utils.ensureDirectory;
import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.gematik.combine.CombineConfiguration;
import io.cucumber.messages.types.Examples;
import io.cucumber.messages.types.Feature;
import io.cucumber.messages.types.FeatureChild;
import io.cucumber.messages.types.GherkinDocument;
import io.cucumber.messages.types.Scenario;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
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

  public Set<ExampleCounter> getExampleCounter() {
    return Collections.unmodifiableSet(exampleCounter);
  }

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
    for (File fileInDir : requireNonNull(file.listFiles())) {
      if (fileInDir.isDirectory()) {
        files.addAll(getFeatureFiles(fileInDir));
      } else {
        files.add(fileInDir);
      }
    }
    return files;
  }

  @SneakyThrows
  private GherkinDocument transformToGherkin(File file) {
    return parseGherkinString(file.toURI().toString(), Files.readString(file.toPath()));
  }

  private void countExecutions(List<GherkinDocument> features) {
    features.stream()
        .map(GherkinDocument::getFeature)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .forEach(this::countScenarios);
  }

  @SneakyThrows
  private void countScenarios(Feature feature) {
    ExampleCounter counter = new ExampleCounter(feature.getName());
    if (exampleCounter.contains(counter)) {
      throw new MojoExecutionException(
          "Could not count features correctly because 2 feature files named the same: "
              + feature.getName());
    }
    exampleCounter.add(counter);
    for (FeatureChild featureChild : feature.getChildren()) {
      featureChild
          .getScenario()
          .ifPresent(scenario -> counter.addScenario(scenario.getName(), countScenarios(scenario)));
    }
  }

  public static int countScenarios(Scenario scenario) {
    if (scenario.getExamples().isEmpty()) { // not a scenario outline
      return 1;
    } else {
      return scenario.getExamples().stream().map(Examples::getTableBody).mapToInt(List::size).sum();
    }
  }

  private void writeExecutionsToFile(CombineConfiguration config) {
    TotalCounter totalCounter = new TotalCounter(exampleCounter);
    File targetDir = ensureDirectory(config.getGeneratedCombineItemsDir());
    if (config.getCountExecutionsFormat().contains(Format.JSON)) {
      createJsonFile(targetDir, totalCounter);
    }
    if (config.getCountExecutionsFormat().contains(Format.TXT)) {
      createTxtFile(targetDir, totalCounter);
    }
  }

  @SneakyThrows
  private void createJsonFile(File targetDir, TotalCounter totalCounter) {
    writeLineToStatisticFile(
        targetDir,
        new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(totalCounter),
        ".json",
        true);
  }

  private void createTxtFile(File targetDir, TotalCounter totalCounter) {
    writeLineToStatisticFile(targetDir, "total -> " + totalCounter.getTotal(), ".txt", true);
    writeLineToStatisticFile(
        targetDir, "totalScenarios -> " + totalCounter.getTotalScenarioAmount(), ".txt", false);
    exampleCounter.stream()
        .forEach(counter -> {
          writeLineToStatisticFile(
              targetDir,
              "\n"
                  + counter.getName()
                  + " -> "
                  + counter.getScenarios().values().stream().reduce(Integer::sum).orElse(0)
                  + " SzenarioAmount => "
                  + counter.getScenarioAmount(),
              ".txt");
          counter
              .getScenarios()
              .forEach((key, value) ->
                  writeLineToStatisticFile(targetDir, "\t" + key + " -> " + value, ".txt"));
        });
  }

  private void writeLineToStatisticFile(File targetDir, String line, String fileEnding) {
    writeLineToStatisticFile(targetDir, line, fileEnding, false);
  }

  @SneakyThrows
  private static void writeLineToStatisticFile(
      File targetDir, String line, String fileEnding, boolean override) {
    FileUtils.write(
        new File(targetDir, COUNT_EXECUTION_FILE_NAME + fileEnding),
        line + "\n",
        StandardCharsets.UTF_8,
        !override);
  }

  public enum Format {
    TXT,
    JSON;

    public static Format fromString(String format) {
      return Format.valueOf(format.toUpperCase());
    }

    @Override
    public String toString() {
      return name().toLowerCase();
    }
  }
}

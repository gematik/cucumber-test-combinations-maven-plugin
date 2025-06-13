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

import static de.gematik.BaseMojo.GENERATED_COMBINE_ITEMS_DIR;
import static de.gematik.combine.count.ExecutionCounter.COUNT_EXECUTION_FILE_NAME;
import static io.cucumber.core.internal.com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import de.gematik.combine.CombineConfiguration;
import io.cucumber.core.internal.com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

class ExecutionCounterTest {

  private final String OUTPUT_DIR = "./src/test/resources/featureFiles/correct";
  private final String OUTPUT_DIR_WRONG = "./src/test/resources/featureFiles/sameName";
  private final String OUTPUT_DIR_EMPTY_SCENARIOS =
      "./src/test/resources/featureFiles/emptyScenarios";

  private ExecutionCounter underTest = new ExecutionCounter();

  @BeforeEach
  void deleteOldFiles() {
    try {
      Arrays.stream(Objects.requireNonNull(new File(GENERATED_COMBINE_ITEMS_DIR).listFiles()))
          .forEach(File::delete);
    } catch (Exception ex) {
      System.err.println(ex);
    }
  }

  private static CombineConfiguration.CombineConfigurationBuilder getBaseCombineConfigurationBuilder() {
    return CombineConfiguration.builder()
        .breakIfTableToSmall(false)
        .minTableSize(1)
        .breakIfMinimalTableError(true)
        .softFilterToHardFilter(false);
  }

  @Test
  @SneakyThrows
  void textAndJsonFileShouldBeCreated() {
    // arrange
    CombineConfiguration config =
        getBaseCombineConfigurationBuilder()
            .countExecutionsFormat(List.of(ExecutionCounter.Format.values()))
            .countExecutions(true)
            .outputDir(OUTPUT_DIR)
            .build();
    // act
    underTest.count(config);
    // assert
    assertThat(new File(GENERATED_COMBINE_ITEMS_DIR).list())
        .containsAll(
            List.of(COUNT_EXECUTION_FILE_NAME + ".txt", COUNT_EXECUTION_FILE_NAME + ".json"));
  }

  @Test
  @SneakyThrows
  void shouldNotCreateFilesIfCountExecutionsSetToFalse() {
    // arrange
    CombineConfiguration config =
        getBaseCombineConfigurationBuilder()
            .countExecutionsFormat(List.of(ExecutionCounter.Format.values()))
            .countExecutions(false)
            .outputDir(OUTPUT_DIR)
            .build();
    // act
    underTest.count(config);
    // assert
    assertThat(new File(GENERATED_COMBINE_ITEMS_DIR)).isEmptyDirectory();
  }

  @Test
  @SneakyThrows
  void onlyTextFileShouldBeCreated() {
    // arrange
    CombineConfiguration config =
        getBaseCombineConfigurationBuilder()
            .countExecutionsFormat(List.of(ExecutionCounter.Format.TXT))
            .countExecutions(true)
            .outputDir(OUTPUT_DIR)
            .build();
    // act
    underTest.count(config);
    // assert
    assertThat(new File(GENERATED_COMBINE_ITEMS_DIR).list())
        .contains(COUNT_EXECUTION_FILE_NAME + ".txt");
    assertThat(new File(GENERATED_COMBINE_ITEMS_DIR).list())
        .doesNotContain(COUNT_EXECUTION_FILE_NAME + ".json");
  }

  @Test
  @SneakyThrows
  void shouldCountCorrectly() {
    // arrange
    CombineConfiguration config =
        getBaseCombineConfigurationBuilder()
            .countExecutionsFormat(List.of(ExecutionCounter.Format.JSON))
            .countExecutions(true)
            .outputDir(OUTPUT_DIR)
            .build();
    // act
    underTest.count(config);
    // assert
    JSONObject testJson =
        new JSONObject(
            Files.readString(
                Path.of(GENERATED_COMBINE_ITEMS_DIR + "/" + COUNT_EXECUTION_FILE_NAME + ".json")));
    assertThat(testJson.get("total")).isEqualTo(33);
    assertThat(testJson.getJSONArray("features").getJSONObject(0).get("total")).isEqualTo(21);
    assertThat(testJson.getJSONArray("features").getJSONObject(1).get("total")).isEqualTo(12);
  }

  @Test
  @SneakyThrows
  void shouldThrowExceptionBecauseFileNamedTheSame() {
    // arrange
    CombineConfiguration config =
        CombineConfiguration.builder()
            .breakIfTableToSmall(false)
            .minTableSize(1)
            .breakIfMinimalTableError(true)
            .softFilterToHardFilter(false)
            .countExecutionsFormat(List.of(ExecutionCounter.Format.JSON))
            .countExecutions(true)
            .outputDir(OUTPUT_DIR_WRONG)
            .build();
    // act
    assertThatThrownBy(() -> underTest.count(config))
        // assert
        .isInstanceOf(MojoExecutionException.class)
        .hasMessage(
            "Could not count features correctly because 2 feature files named the same: SameName");
  }

  @Test
  @SneakyThrows
  void shouldNotCountEmptyScenarios() {
    // arrange
    CombineConfiguration config =
        CombineConfiguration.builder()
            .breakIfTableToSmall(false)
            .minTableSize(1)
            .breakIfMinimalTableError(true)
            .softFilterToHardFilter(false)
            .countExecutionsFormat(List.of(ExecutionCounter.Format.values()))
            .countExecutions(true)
            .outputDir(OUTPUT_DIR_EMPTY_SCENARIOS)
            .build();
    // act
    underTest.count(config);
    String counterString =
        FileUtils.readFileToString(
            new File(GENERATED_COMBINE_ITEMS_DIR + "/" + COUNT_EXECUTION_FILE_NAME + ".json"), UTF_8);
    TotalCounter tc =
        new ObjectMapper()
            .configure(FAIL_ON_UNKNOWN_PROPERTIES, false)
            .readValue(counterString, TotalCounter.class);
    // assert
    assertThat(tc.getTotalScenarioAmount()).isEqualTo(3);
    assertThat(
            tc.getFeatures().stream()
                .map(ExampleCounter::getScenarios)
                .map(Map::values)
                .mapToLong(Collection::size)
                .sum())
        .isEqualTo(5);
  }
}

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

import de.gematik.combine.count.ExampleCounter;
import de.gematik.combine.count.ExecutionCounter;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.File;

class SkipNonRequiredScenariosTest extends AbstractCombineMojoTest {

  @AfterEach
  void tearDown() {
    System.clearProperty("cucumber.filter.tags");
  }

  @Test
  @SneakyThrows
  void shouldSkipFeature() {
    System.setProperty("cucumber.filter.tags", "@NotRequired");
    // act
    combineMojo.execute();
    // assert
    assertThat(new File(outputDir())).isEmptyDirectory();

    ExecutionCounter executionCounter = combineMojo.getExecutionCounter();

    assertThat(executionCounter.getExampleCounter()).isEmpty();
  }

  @Test
  void shouldIncludeFeature() {
    System.setProperty("cucumber.filter.tags", "@Feature");
    // act
    combineMojo.execute();
    // assert
    ExecutionCounter executionCounter = combineMojo.getExecutionCounter();

    var required = new ExampleCounter("Feature");
    required.addScenario("Scenario1", 1);
    required.addScenario("Scenario2", 0);
    required.addScenario("Scenario3", 1);
    required.addScenario("Scenario4", 1);

    assertThat(executionCounter.getExampleCounter()).contains(required);
  }

  @Test
  void shouldIncludeScenariosWithoutSkipTag() {
    System.setProperty("cucumber.filter.tags", "@Scenario1");
    // act
    combineMojo.execute();
    // assert

    ExecutionCounter executionCounter = combineMojo.getExecutionCounter();

    var required = new ExampleCounter("Feature");
    required.addScenario("Scenario1", 1);
    required.addScenario("Scenario2", 0);
    required.addScenario("Scenario3", 1); // not an outline
    required.addScenario("Scenario4", 0);

    assertThat(executionCounter.getExampleCounter()).contains(required);
  }

  @Override
  protected boolean countExecutions() {
    return true;
  }

  @Override
  protected String combineItemsFile() {
    return "./src/test/resources/input/input4.json";
  }
}

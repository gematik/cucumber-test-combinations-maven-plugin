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

import java.util.Map;
import java.util.TreeMap;

import io.cucumber.core.internal.com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

@Getter
@EqualsAndHashCode
@NoArgsConstructor
@JsonPropertyOrder({
    "name",
    "scenarios",
    "scenarioAmount",
    "total"
})
public class ExampleCounter implements Comparable<ExampleCounter> {

  private String name;
  private Map<String, Integer> scenarios;

  public ExampleCounter(String name) {
    this.name = name;
    this.scenarios = new TreeMap<>();
  }

  public Integer getTotal() {
    return scenarios.values().stream().reduce(Integer::sum).orElse(0);
  }

  public long getScenarioAmount() {
    return scenarios.entrySet().stream().filter(e -> e.getValue() > 0).count();
  }

  public void addScenario(String name, int size) {
    scenarios.put(name, size);
  }

  @Override
  public int compareTo(@NotNull ExampleCounter exampleCounter) {
    return this.name.compareTo(exampleCounter.name);
  }

}

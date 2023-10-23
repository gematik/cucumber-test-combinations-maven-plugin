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

import java.util.Map;
import java.util.TreeMap;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

@Getter
@EqualsAndHashCode
@RequiredArgsConstructor
public class ExampleCounter implements Comparable<ExampleCounter> {

  private final String name;
  private Map<String, Integer> scenarios = new TreeMap<>();

  public Integer getTotal() {
    return scenarios.values().stream().reduce(Integer::sum).orElse(0);
  }

  public void addScenario(String name, int size) {
    scenarios.put(name, size);
  }

  @Override
  public int compareTo(@NotNull ExampleCounter exampleCounter) {
    return this.name.compareTo(exampleCounter.name);
  }

}

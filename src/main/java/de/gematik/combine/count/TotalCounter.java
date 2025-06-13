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

import java.util.Set;

import io.cucumber.core.internal.com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@JsonPropertyOrder({
    "features",
    "total",
    "totalScenarioAmount"
})
public class TotalCounter {

  private Set<ExampleCounter> features;

  public TotalCounter(Set<ExampleCounter> exampleCounters) {
    this.features = exampleCounters;
  }

  public int getTotal() {
    return features.stream().map(ExampleCounter::getTotal).reduce(Integer::sum).orElse(0);
  }

  public long getTotalScenarioAmount(){
    return features.stream().map(ExampleCounter::getScenarioAmount).reduce(Long::sum).orElse(0L);
  }

}

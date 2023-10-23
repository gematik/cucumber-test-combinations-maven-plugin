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

package de.gematik.prepare.pooling.strategies;

import java.util.List;
import java.util.Set;

public abstract class MatchStrategy {

  public static final String MATCHING = "contains";
  public static final String NOT_MATCHING = "notContaining";

  protected List<String> pattern;

  protected MatchStrategy(List<String> pattern) {
    this.pattern = pattern;
  }

  protected abstract boolean doesMatch(String group);

  public String match(Set<String> item) {
    for (String itemGroup : item) {
      if (doesMatch(itemGroup)) {
        return MATCHING;
      }
    }
    return NOT_MATCHING;
  }
}

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
import java.util.stream.Collectors;

public class CaseInsensitiveStrategy extends MatchStrategy {

  public CaseInsensitiveStrategy(List<String> groups) {
    super(groups.stream().map(String::toLowerCase).collect(Collectors.toList()));
  }

  @Override
  protected boolean doesMatch(String group) {
    return this.pattern.stream().anyMatch(e -> group.toLowerCase().contains(e));
  }
}

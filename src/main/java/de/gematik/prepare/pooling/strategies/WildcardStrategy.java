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
import org.apache.commons.lang3.StringUtils;

public class WildcardStrategy extends MatchStrategy {


  public WildcardStrategy(List<String> groups) {
    super(groups.stream().map(String::toLowerCase).collect(Collectors.toList()));
  }

  @Override
  protected boolean doesMatch(String group) {
    return pattern.stream().anyMatch(e -> match(e, group));
  }

  private boolean match(String group, String itemGroup) {
    itemGroup = itemGroup.toLowerCase();
    List<String> parts = List.of(group.split("\\*")).stream().filter(StringUtils::isNotBlank)
        .collect(
            Collectors.toList());
    if (!group.startsWith("*") && !itemGroup.startsWith(parts.get(0))) {
      return false;
    }
    if (!group.endsWith("*") && !itemGroup.endsWith(parts.get(parts.size() - 1))) {
      return false;
    }
    for (String p : parts) {
      if (itemGroup.contains(p)) {
        itemGroup = itemGroup.replaceFirst(p, "");
      } else {
        return false;
      }
    }
    return true;
  }
}

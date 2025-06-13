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

package de.gematik.prepare.pooling;

import static de.gematik.prepare.pooling.strategies.MatchStrategy.MATCHING;

import de.gematik.combine.model.CombineItem;
import de.gematik.prepare.pooling.strategies.CaseInsensitiveExactStrategy;
import de.gematik.prepare.pooling.strategies.CaseInsensitiveStrategy;
import de.gematik.prepare.pooling.strategies.CaseSensitiveExactStrategy;
import de.gematik.prepare.pooling.strategies.CaseSensitiveStrategy;
import de.gematik.prepare.pooling.strategies.MatchStrategy;
import de.gematik.prepare.pooling.strategies.RegexStrategy;
import de.gematik.prepare.pooling.strategies.WildcardStrategy;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class GroupMatcher {

  private List<String> groups;
  private GroupMatchStrategyType strategyType;

  public String order(CombineItem combineItem) {
    return this.order(combineItem.getGroups());
  }

  public String order(Set<String> groups) {
    return getStrategy().match(groups);
  }

  public List<String> getAllMatchingGroups(List<CombineItem> matchingItems) {
    return getAllMatchingGroups(
        matchingItems.stream()
            .map(CombineItem::getGroups)
            .flatMap(Collection::stream)
            .collect(Collectors.toSet()));
  }

  public List<String> getAllMatchingGroups(Set<String> groups) {
    MatchStrategy strategy = getStrategy();
    return groups.stream()
        .filter(e -> strategy.match(Set.of(e)).equals(MATCHING))
        .collect(Collectors.toList());
  }

  public List<CombineItem> getAllItemsMatching(List<CombineItem> allItems) {
    return allItems.stream()
        .filter(item -> this.order(item).equals(MATCHING))
        .collect(Collectors.toList());
  }

  private MatchStrategy getStrategy() {
    switch (strategyType) {
      case CASE_INSENSITIVE:
        return new CaseInsensitiveStrategy(groups);
      case CASE_INSENSITIVE_EXACT:
        return new CaseInsensitiveExactStrategy(groups);
      case CASE_SENSITIVE:
        return new CaseSensitiveStrategy(groups);
      case CASE_SENSITIVE_EXACT:
        return new CaseSensitiveExactStrategy(groups);
      case REGEX:
        return new RegexStrategy(groups);
      default:
        return new WildcardStrategy(groups);
    }
  }
}

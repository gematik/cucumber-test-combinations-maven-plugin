/*
 * Copyright 20023 gematik GmbH
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

package de.gematik.prepare.pooling;

import static de.gematik.prepare.pooling.strategies.MatchStrategy.MATCHING;
import static de.gematik.utils.Utils.getItemsToCombine;
import static java.lang.String.format;

import de.gematik.combine.model.CombineItem;
import de.gematik.prepare.PrepareItemsConfig;
import de.gematik.prepare.PrepareItemsMojo;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.maven.plugin.MojoExecutionException;

@NoArgsConstructor
public class Pooler {

  private final Random random = new Random();

  @Setter
  private PrepareItemsConfig config;


  public Pooler(PrepareItemsConfig config) {
    this.config = config;
  }

  public List<CombineItem> pool() throws MojoExecutionException {
    List<CombineItem> allCombineItems = getItemsToCombine(new File(config.getCombineItemsFile()),
        PrepareItemsMojo.getInstance(), false);
    allCombineItems.removeAll(
        allCombineItems.stream()
            .filter(
                e -> getMatcher(config.getExcludedGroups()).order(e.getGroups()).equals(MATCHING))
            .collect(Collectors.toList()));
    Set<CombineItem> items = getNeededItemsFromGroup(allCombineItems);

    if (config.getPoolSize() <= 0 && !items.isEmpty()) {
      return new ArrayList<>(items);
    }

    allCombineItems.removeAll(items);
    doChecks(allCombineItems, items);

    return new ArrayList<>(fillUpList(allCombineItems, items));
  }

  private Set<CombineItem> getNeededItemsFromGroup(final List<CombineItem> allCombineItems)
      throws MojoExecutionException {
    Set<CombineItem> items = new HashSet<>();

    for (PoolGroup poolGroup : config.getPoolGroups()) {
      items.addAll(getRequestedAmountFromPoolGroup(poolGroup, allCombineItems));
    }
    return items;
  }

  private Collection<? extends CombineItem> getRequestedAmountFromPoolGroup(PoolGroup poolGroup,
      List<CombineItem> allCombineItems) throws
      MojoExecutionException {

    GroupMatcher matcher = getMatcher(poolGroup);
    List<CombineItem> selectedItems = new ArrayList<>();

    List<CombineItem> matchingItems = matcher.getAllItemsMatching(allCombineItems);
    List<String> matchingGroups = matcher.getAllMatchingGroups(matchingItems);
    if (matchingGroups.size() < poolGroup.getAmount()) {
      throw new MojoExecutionException(format(
          "Requested %s for group(s) %s but only found %s matching group(s) (%s) with matching strategy %s",
          poolGroup.getAmount(), poolGroup.getGroupPattern(), matchingGroups.size(), matchingGroups,
          poolGroup.getStrategy()));
    }
    if (poolGroup.getAmount() < 1) {
      return allCombineItems.stream().filter(item -> matcher.order(item).equals(MATCHING))
          .collect(Collectors.toSet());
    }
    IntStream.range(0, poolGroup.getAmount()).forEach(i -> {
      String randomGroup = selectRandomGroup(matchingItems, matcher);
      List<CombineItem> randomSelectedItems = matchingItems.stream()
          .filter(item -> item.getGroups().contains(randomGroup)).collect(Collectors.toList());
      matchingItems.removeAll(randomSelectedItems);
      selectedItems.addAll(randomSelectedItems);
    });
    return selectedItems;
  }

  private String selectRandomGroup(List<CombineItem> matchingItems, GroupMatcher matcher) {
    List<String> validGroups = matcher.getAllMatchingGroups(matchingItems);
    return validGroups.get(random.nextInt(validGroups.size()));
  }

  private Set<CombineItem> fillUpList(List<CombineItem> leftItems, Set<CombineItem> items) {
    if (config.getPoolSize() < 1) {
      return new HashSet<>(leftItems);
    }
    while (countGroups(items) < config.getPoolSize() || items.size() < config.getPoolSize()) {
      CombineItem randomItem = leftItems.get(random.nextInt(leftItems.size()));
      if (randomItem.getGroups().isEmpty()) {
        items.add(randomItem);
        leftItems.remove(randomItem);
        continue;
      }
      String randomGroup = new ArrayList<>(randomItem.getGroups())
          .get(random.nextInt(randomItem.getGroups().size()));
      List<CombineItem> newItems = leftItems.stream()
          .filter(i -> getMatcher(List.of(randomGroup)).order(i).equals(MATCHING))
          .collect(Collectors.toList());
      items.addAll(newItems);
      leftItems.removeAll(newItems);
    }
    return items;
  }

  private void doChecks(List<CombineItem> leftItems, Set<CombineItem> items)
      throws MojoExecutionException {
    if (countGroups(leftItems) + countGroups(items) < config.getPoolSize()) {
      throw new MojoExecutionException(
          "PoolSize to high! Could not find sufficient item-groups to fulfill request");
    }
  }

  private long countGroups(Collection<CombineItem> itemList) {
    long itemsWithoutGroup = itemList.stream().filter(i -> i.getGroups().isEmpty()).count();
    long sumGroupsFromItems = itemList.stream().map(CombineItem::getGroups)
        .flatMap(Collection::stream).collect(Collectors.toSet()).size();
    return sumGroupsFromItems + itemsWithoutGroup;
  }

  private GroupMatcher getMatcher(PoolGroup poolGroup) {
    return poolGroup.getStrategy() == null ? getMatcher(poolGroup.getGroupPattern())
        : getMatcher(poolGroup.getStrategy(), poolGroup.getGroupPattern());
  }

  private GroupMatcher getMatcher(List<String> groups) {
    return getMatcher(config.getDefaultMatchStrategy(), groups);
  }

  private GroupMatcher getMatcher(GroupMatchStrategyType strategy, List<String> groups) {
    return new GroupMatcher(groups, strategy);
  }
}

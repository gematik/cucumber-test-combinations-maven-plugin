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

package de.gematik.prepare;

import static de.gematik.prepare.PrepareItemsMojo.GENERATED_COMBINE_ITEMS_DIR;
import static de.gematik.utils.Utils.getItemsToCombine;
import static java.nio.charset.StandardCharsets.UTF_8;

import de.gematik.combine.model.CombineItem;
import io.cucumber.core.internal.com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import net.minidev.json.JSONObject;
import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.MojoExecutionException;

@AllArgsConstructor
@NoArgsConstructor
public class Pooler {

  private static final String CONTAINING = "contains";
  private static final String NOT_CONTAINING = "notContaining";
  private static final String EXCLUDED = "excluded";
  private final Random random = new Random();


  @Setter
  private PrepareItemsConfig config;

  public List<CombineItem> pool() throws MojoExecutionException {
    Map<String, List<CombineItem>> filteredMap = getItemsToCombine(
        new File(config.getCombineItemsFile()),
        PrepareItemsMojo.getInstance(), false).stream()
        .collect(Collectors.groupingBy(this::order));

    doChecks(filteredMap);

    if (config.getGroups().isEmpty() && config.getPoolSize() <= 0) {
      return getAllPossibleItems(filteredMap);
    }
    if (filteredMap.get(CONTAINING).size() == config.getPoolSize()) {
      return filteredMap.get(CONTAINING);
    }
    return fillUpList(filteredMap);
  }

  private List<CombineItem> fillUpList(Map<String, List<CombineItem>> filteredMap) {
    List<CombineItem> finalListOfItems = filteredMap.get(CONTAINING);
    List<CombineItem> leftOvers = filteredMap.get(NOT_CONTAINING);
    while (countGroups(finalListOfItems) < config.getPoolSize()
        || finalListOfItems.size() < config.getPoolSize()) {
      CombineItem randomSelectedItem = leftOvers.get(random.nextInt(leftOvers.size()));
      if (randomSelectedItem.getGroups().isEmpty()) {
        finalListOfItems.add(randomSelectedItem);
        leftOvers.remove(randomSelectedItem);
        continue;
      }
      String randomSelectedGroup = (String) randomSelectedItem.getGroups().toArray()[
          random.nextInt(randomSelectedItem.getGroups().size())];
      List<CombineItem> allItemsOfRandomSelectedGroup = leftOvers.stream()
          .filter(i -> i.getGroups().contains(randomSelectedGroup)).collect(Collectors.toList());
      finalListOfItems.addAll(allItemsOfRandomSelectedGroup);
      leftOvers.removeAll(allItemsOfRandomSelectedGroup);
    }
    writeUsedGroupsToFile(finalListOfItems);
    return finalListOfItems;
  }

  @SneakyThrows
  private void writeUsedGroupsToFile(List<CombineItem> finalListOfItems) {
    Set<String> usedGroups = finalListOfItems.stream().map(CombineItem::getGroups)
        .flatMap(Collection::stream).collect(Collectors.toSet());
    List<String> excludedGroups = config.getExcludedGroups();
    List<CombineItem> usedWithoutGroups = finalListOfItems.stream()
        .filter(e -> e.getGroups().isEmpty()).collect(Collectors.toList());
    JSONObject result = new JSONObject();
    result.put("usedGroups", usedGroups);
    result.put("usedItemsWithoutGroup",
        usedWithoutGroups.stream().map(CombineItem::getValue).collect(
            Collectors.toList()));
    result.put("excludedGroups", excludedGroups);
    FileUtils.writeStringToFile(
        new File(GENERATED_COMBINE_ITEMS_DIR + File.separator + "usedGroups.json"),
        new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(result), UTF_8);
  }

  private List<CombineItem> getAllPossibleItems(Map<String, List<CombineItem>> filteredMap) {
    return filteredMap.entrySet().stream().filter(e ->
            e.getKey().equals(CONTAINING) || e.getKey().equals(NOT_CONTAINING))
        .map(Entry::getValue).flatMap(List::stream).collect(Collectors.toList());
  }

  private String order(CombineItem combineItem) {
    for (String itemGroup : combineItem.getGroups()) {
      if (config.getExcludedGroups().contains(itemGroup)) {
        return EXCLUDED;
      }
      if (config.getGroups().contains(itemGroup)) {
        return CONTAINING;
      }
    }
    return NOT_CONTAINING;
  }

  private void doChecks(Map<String, List<CombineItem>> filteredMap) throws MojoExecutionException {
    if (config.getGroups().size() > config.getPoolSize() && config.getPoolSize() > 0) {
      throw new MojoExecutionException(
          "The given amount of groups is higher than the requested poolSize");
    }

    List<CombineItem> allValidItems = new ArrayList<>();
    filteredMap.computeIfAbsent(CONTAINING, k -> new ArrayList<>());
    filteredMap.computeIfAbsent(NOT_CONTAINING, k -> new ArrayList<>());
    allValidItems.addAll(filteredMap.get(NOT_CONTAINING));
    allValidItems.addAll(filteredMap.get(CONTAINING));

    if (countGroups(allValidItems) < config.getPoolSize()) {
      throw new MojoExecutionException(
          "PoolSize to high! Could not find sufficient item-groups to fulfill request");
    }
  }

  private long countGroups(List<CombineItem> itemList) {
    long itemsWithoutGroup = itemList.stream().filter(i -> i.getGroups().isEmpty()).count();
    long sumGroupsFromItems = itemList.stream().map(CombineItem::getGroups)
        .flatMap(Collection::stream).collect(Collectors.toSet()).size();
    return sumGroupsFromItems + itemsWithoutGroup;
  }
}

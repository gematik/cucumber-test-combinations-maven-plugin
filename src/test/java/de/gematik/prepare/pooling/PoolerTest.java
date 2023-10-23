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

package de.gematik.prepare.pooling;

import static de.gematik.prepare.pooling.GroupMatchStrategyType.CASE_INSENSITIVE;
import static de.gematik.prepare.pooling.GroupMatchStrategyType.CASE_INSENSITIVE_EXACT;
import static de.gematik.prepare.pooling.GroupMatchStrategyType.CASE_SENSITIVE;
import static de.gematik.prepare.pooling.GroupMatchStrategyType.CASE_SENSITIVE_EXACT;
import static de.gematik.prepare.pooling.GroupMatchStrategyType.REGEX;
import static de.gematik.prepare.pooling.GroupMatchStrategyType.WILDCARD;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import de.gematik.combine.model.CombineItem;
import de.gematik.prepare.AbstractPrepareTest;
import de.gematik.prepare.PrepareItemsConfig;
import de.gematik.prepare.PrepareItemsConfig.PrepareItemsConfigBuilder;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import lombok.SneakyThrows;
import org.apache.maven.plugin.MojoExecutionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class PoolerTest extends AbstractPrepareTest {

  public static final String ALL_WITH_GROUP_SET = "allWithGroupsSet.json";
  public static final String FOUR_WITH_GROUP_SET_AND_ONE_WITHOUT = "fourWithGroupsSetAndOneWithout.json";
  public static final String POOLING_GROUP_DIFFERENT_MATCHER = "poolingGroupDifferentMatcher.json";
  public static final String ONE_SINGLE_GROUP_AND_ONE_DOUBLE_GROUP = "oneSingleGroupAndOneDoubleGroup.json";
  public static final String TWO_WITH_AND_TWO_WITHOUT_GROUP = "twoWithAndTwoWithoutGroup.json";
  public static final String ONE_WITH_TWO_GROUPS = "oneWithTwoGroups.json";
  public static final String WITH_LONG_NAMES_FOR_PATTERN = "withLongNamesForPattern.json";
  private static final String WITH_LONG_GROUPS_FOR_POOLGROUP_TESTS = "withLongGroupsForPoolgroupTests.json";
  private final String baseInputPath = "./src/test/resources/input/groups/".replace("/",
      File.separator);

  private Pooler pooler = new Pooler();
  private PrepareItemsConfigBuilder configBuilder;

  @BeforeEach
  void createBasicConfigBuilder() {
    configBuilder = PrepareItemsConfig.builder()
        .excludedGroups(new ArrayList<>())
        .poolGroups(new ArrayList<>())
        .poolGroups(new ArrayList<>())
        .defaultMatchStrategy(GroupMatchStrategyType.REGEX);
  }

  @Test
  @SneakyThrows
  void returnAll() {
    PrepareItemsConfig config = configBuilder
        .combineItemsFile(baseInputPath + "noProfileSet.json")
        .poolGroups(List.of())
        .build();
    pooler.setConfig(config);

    List<CombineItem> res = pooler.pool();
    assertThat(res).hasSize(4);
  }

  private static Stream<Arguments> returnAllItemsHavingGroup() {
    Map<String, Integer> map = Map.of("A", 4, "B", 3, "C", 2, "D", 1);
    return Stream.of(
        arguments(List.of("A"), map),
        arguments(List.of("A", "B"), map),
        arguments(List.of("A", "B", "C"), map),
        arguments(List.of("A", "B", "D"), map),
        arguments(List.of("A", "C", "D"), map),
        arguments(List.of("A", "B", "C", "D"), map),
        arguments(List.of("A", "C"), map),
        arguments(List.of("A", "D"), map),
        arguments(List.of("B", "C"), map),
        arguments(List.of("B", "D"), map),
        arguments(List.of("C", "D"), map)
    );
  }

  @SneakyThrows
  @MethodSource
  @ParameterizedTest
  void returnAllItemsHavingGroup(List<String> groups, Map<String, Integer> sizeMap) {
    PoolGroup poolGroup = PoolGroup.builder().groupPattern(groups).build();
    PrepareItemsConfig config = configBuilder
        .combineItemsFile(baseInputPath + ALL_WITH_GROUP_SET)
        .poolGroups(List.of(poolGroup))
        .build();
    pooler.setConfig(config);

    List<CombineItem> res = pooler.pool();
    assertThat(res).hasSize(groups.stream().map(sizeMap::get).reduce(0, Integer::sum));
  }

  private static Stream<Arguments> returnRequestedAmountItemsHavingGroup() {
    Map<String, Integer> map = Map.of("A", 4, "B", 3, "C", 2, "D", 1);
    return Stream.of(
        arguments(List.of("A"), map),
        arguments(List.of("A", "B"), map),
        arguments(List.of("A", "B", "C"), map),
        arguments(List.of("A", "B", "D"), map),
        arguments(List.of("A", "C", "D"), map),
        arguments(List.of("A", "B", "C", "D"), map),
        arguments(List.of("A", "C"), map),
        arguments(List.of("A", "D"), map),
        arguments(List.of("B", "C"), map),
        arguments(List.of("B", "D"), map),
        arguments(List.of("C", "D"), map)
    );
  }

  @SneakyThrows
  @MethodSource
  @ParameterizedTest
  void returnRequestedAmountItemsHavingGroup(List<String> groups, Map<String, Integer> sizeMap) {
    PoolGroup poolGroup = PoolGroup.builder()
        .groupPattern(groups)
        .build();
    PrepareItemsConfig config = configBuilder
        .combineItemsFile(baseInputPath + FOUR_WITH_GROUP_SET_AND_ONE_WITHOUT)
        .poolGroups(List.of(poolGroup))
        .poolSize(groups.size() + 1)
        .build();
    pooler.setConfig(config);

    List<CombineItem> res = pooler.pool();
    assertThat(res)
        .hasSizeGreaterThan(groups.stream().map(sizeMap::get).reduce(0, Integer::sum));
  }

  @Test
  @SneakyThrows
  void fillWithRandomProfileIfListSmallerThanPoolSize() {
    PoolGroup poolGroup = PoolGroup.builder()
        .groupPattern(List.of("A"))
        .build();
    PrepareItemsConfig config = configBuilder
        .combineItemsFile(baseInputPath + ONE_SINGLE_GROUP_AND_ONE_DOUBLE_GROUP)
        .poolGroups(List.of(poolGroup))
        .poolSize(2)
        .build();
    pooler.setConfig(config);

    List<CombineItem> res = pooler.pool();
    Set<String> usedGroups = res.stream().map(CombineItem::getGroups)
        .reduce((v1, v2) -> {
          v1.addAll(v2);
          return v1;
        }).orElseThrow();
    assertThat(usedGroups).hasSize(2);
    assertThat(res).hasSize(3);
  }

  @Test
  @SneakyThrows
  void shouldUseItemWithoutGroupToFill() {
    PoolGroup poolGroup = PoolGroup.builder()
        .groupPattern(List.of("A", "B"))
        .build();
    PrepareItemsConfig config = configBuilder
        .combineItemsFile(baseInputPath + TWO_WITH_AND_TWO_WITHOUT_GROUP)
        .poolGroups(List.of(poolGroup))
        .poolSize(3)
        .build();
    pooler.setConfig(config);

    List<CombineItem> res = pooler.pool();
    assertThat(res).hasSize(3);
  }

  @Test
  @SneakyThrows
  void shouldCountTwoGroupsOfOneItemAsOnlyOneItemAndContinueFilling() {
    PoolGroup poolGroup = PoolGroup.builder()
        .groupPattern(List.of("A"))
        .build();
    PrepareItemsConfig config = configBuilder
        .combineItemsFile(baseInputPath + ONE_WITH_TWO_GROUPS)
        .poolGroups(List.of(poolGroup))
        .poolSize(2)
        .build();
    pooler.setConfig(config);

    List<CombineItem> res = pooler.pool();
    assertThat(res).hasSize(2);

  }

  @Test
  @SneakyThrows
  void shouldContinueFillingIfItemIsInBothGroups() {
    PoolGroup poolGroup = PoolGroup.builder()
        .groupPattern(List.of("A", "B"))
        .build();
    PrepareItemsConfig config = configBuilder
        .combineItemsFile(baseInputPath + ONE_WITH_TWO_GROUPS)
        .poolGroups(List.of(poolGroup))
        .poolSize(2)
        .build();
    pooler.setConfig(config);

    List<CombineItem> res = pooler.pool();
    assertThat(res).hasSize(2);
  }

  @Test
  @SneakyThrows
  void expectExceptionIfExcludeDoesNotMakeItPossibleToFulfillPoolSize() {
    PoolGroup poolGroup = PoolGroup.builder()
        .groupPattern(List.of("A"))
        .build();
    PrepareItemsConfig config = configBuilder
        .combineItemsFile(baseInputPath + TWO_WITH_AND_TWO_WITHOUT_GROUP)
        .poolGroups(List.of(poolGroup))
        .excludedGroups(List.of("B"))
        .poolSize(4)
        .build();
    pooler.setConfig(config);

    assertThatThrownBy(() -> pooler.pool()).isInstanceOf(MojoExecutionException.class)
        .hasMessage("PoolSize to high! Could not find sufficient item-groups to fulfill request");
  }

  @Test
  @SneakyThrows
  void shouldCountEveryItemWithoutGroupAsSinglePossibleItem() {
    PoolGroup poolGroup = PoolGroup.builder()
        .groupPattern(List.of("A"))
        .build();
    PrepareItemsConfig config = configBuilder
        .combineItemsFile(baseInputPath + TWO_WITH_AND_TWO_WITHOUT_GROUP)
        .poolGroups(List.of(poolGroup))
        .excludedGroups(List.of("B"))
        .poolSize(3)
        .build();
    pooler.setConfig(config);

    List<CombineItem> res = pooler.pool();
    assertThat(res).hasSize(3);
  }

  @Test
  @SneakyThrows
  void shouldReturnAllPossibleFilteredWithExcluded() {
    PrepareItemsConfig config = configBuilder
        .combineItemsFile(baseInputPath + TWO_WITH_AND_TWO_WITHOUT_GROUP)
        .excludedGroups(List.of("B"))
        .poolSize(0)
        .build();
    pooler.setConfig(config);

    List<CombineItem> res = pooler.pool();
    assertThat(res).hasSize(3);
  }

  @Test
  @SneakyThrows
  void shouldPoolWithCaseSensitive() {
    PoolGroup poolGroup = PoolGroup.builder()
        .groupPattern(List.of("approved"))
        .strategy(CASE_SENSITIVE)
        .build();
    PrepareItemsConfig config = configBuilder
        .combineItemsFile(baseInputPath + WITH_LONG_NAMES_FOR_PATTERN)
        .poolGroups(List.of(poolGroup))
        .defaultMatchStrategy(CASE_SENSITIVE)
        .excludedGroups(List.of("A", "B", "tem"))
        .poolSize(2)
        .build();
    pooler.setConfig(config);

    List<CombineItem> res = pooler.pool();
    assertThat(res).hasSize(2);
    assertThat(res).extracting("value").containsExactlyInAnyOrder("Api7", "Api5");
  }

  @Test
  @SneakyThrows
  void shouldPoolWithCaseInsensitive() {
    PoolGroup poolGroup = PoolGroup.builder()
        .groupPattern(List.of("approved"))
        .strategy(CASE_INSENSITIVE)
        .build();
    PrepareItemsConfig config = configBuilder
        .combineItemsFile(baseInputPath + WITH_LONG_NAMES_FOR_PATTERN)
        .poolGroups(List.of(poolGroup))
        .defaultMatchStrategy(CASE_INSENSITIVE)
        .excludedGroups(List.of("invalid"))
        .poolSize(3)
        .build();
    pooler.setConfig(config);

    List<CombineItem> res = pooler.pool();
    assertThat(res).hasSize(5);
    assertThat(res).extracting("value")
        .containsExactlyInAnyOrder("Api1", "Api3", "Api7", "Api8", "Api9");
  }

  @Test
  @SneakyThrows
  void shouldPoolWithCaseSensitiveExact() {
    PoolGroup poolGroup = PoolGroup.builder()
        .groupPattern(List.of("approved"))
        .strategy(CASE_SENSITIVE_EXACT)
        .build();
    PrepareItemsConfig config = configBuilder
        .combineItemsFile(baseInputPath + WITH_LONG_NAMES_FOR_PATTERN)
        .poolGroups(List.of(poolGroup))
        .defaultMatchStrategy(CASE_SENSITIVE_EXACT)
        .excludedGroups(List.of("D_appRoved_temp", "C_apProved", "B_invalid", "B_not", "A_not"))
        .poolSize(3)
        .build();
    pooler.setConfig(config);

    List<CombineItem> res = pooler.pool();
    assertThat(res).hasSize(3);
    assertThat(res).extracting("value").containsExactlyInAnyOrder("Api1", "Api3", "Api5");
  }

  @Test
  @SneakyThrows
  void shouldPoolWithCaseInsensitiveExact() {
    PoolGroup poolGroup = PoolGroup.builder()
        .groupPattern(List.of("b_not", "b_approved", "a_approved"))
        .strategy(CASE_INSENSITIVE_EXACT)
        .build();
    PrepareItemsConfig config = configBuilder
        .combineItemsFile(baseInputPath + WITH_LONG_NAMES_FOR_PATTERN)
        .poolGroups(List.of(poolGroup))
        .excludedGroups(List.of("D_approved_TEMP"))
        .poolSize(3)
        .build();
    pooler.setConfig(config);

    List<CombineItem> res = pooler.pool();
    assertThat(res).hasSize(3);
    assertThat(res).extracting("value").containsExactlyInAnyOrder("Api1", "Api3", "Api4");
  }

  @Test
  @SneakyThrows
  void shouldActWithDifferentStrategies() {
    PoolGroup p1 = PoolGroup.builder()
        .groupPattern(List.of("*approved"))
        .amount(2)
        .strategy(WILDCARD)
        .build();
    PoolGroup p2 = PoolGroup.builder()
        .groupPattern(List.of(".*new"))
        .strategy(REGEX)
        .amount(1)
        .build();
    PrepareItemsConfig config = configBuilder
        .combineItemsFile(baseInputPath + WITH_LONG_GROUPS_FOR_POOLGROUP_TESTS)
        .poolGroups(List.of(p1, p2))
        .build();
    pooler.setConfig(config);

    List<CombineItem> res = pooler.pool();
    assertThat(res).hasSize(7);
  }

  @Test
  @SneakyThrows
  void shouldThrowExceptionIfCouldNotFindMatchesForOneGroup() {
    PoolGroup p1 = PoolGroup.builder()
        .groupPattern(List.of("*approved"))
        .amount(2)
        .strategy(WILDCARD)
        .build();
    PoolGroup p2 = PoolGroup.builder()
        .groupPattern(List.of("_*new"))
        .strategy(REGEX)
        .amount(1)
        .build();
    PrepareItemsConfig config = configBuilder
        .combineItemsFile(baseInputPath + WITH_LONG_GROUPS_FOR_POOLGROUP_TESTS)
        .poolGroups(List.of(p1, p2))
        .build();
    pooler.setConfig(config);

    assertThatThrownBy(() -> pooler.pool()).isInstanceOf(MojoExecutionException.class)
        .hasMessage(
            "Requested 1 for group(s) [_*new] but only found 0 matching group(s) ([]) with matching strategy REGEX");
  }

  @Test
  @SneakyThrows
  void shouldFindEnoughWithTwoGroupsNamed() {
    PoolGroup poolGroup = PoolGroup.builder()
        .groupPattern(List.of("*new", "*special"))
        .amount(2)
        .strategy(WILDCARD)
        .build();
    PrepareItemsConfig config = configBuilder
        .combineItemsFile(baseInputPath + WITH_LONG_GROUPS_FOR_POOLGROUP_TESTS)
        .poolGroups(List.of(poolGroup))
        .defaultMatchStrategy(WILDCARD)
        .excludedGroups(List.of("*ignore"))
        .build();
    pooler.setConfig(config);

    List<CombineItem> res = pooler.pool();
    res.sort(CombineItem::compareTo);
    assertThat(res).extracting("value").containsExactlyInAnyOrder("Api5", "Api6", "Api7", "Api14");
  }

  @Test
  @SneakyThrows
  void shouldIgnoreExcludedBecauseOfCaseSensitive() {
    PoolGroup poolGroup = PoolGroup.builder()
        .groupPattern(List.of("approved"))
        .strategy(CASE_SENSITIVE)
        .build();
    PrepareItemsConfig config = configBuilder
        .combineItemsFile(baseInputPath + POOLING_GROUP_DIFFERENT_MATCHER)
        .poolGroups(List.of(poolGroup))
        .defaultMatchStrategy(CASE_SENSITIVE)
        .excludedGroups(List.of("verified"))
        .build();
    pooler.setConfig(config);

    List<CombineItem> res = pooler.pool();
    res.sort(CombineItem::compareTo);
    assertThat(res).extracting("value").containsExactlyInAnyOrder("Api1", "Api2");
  }
}
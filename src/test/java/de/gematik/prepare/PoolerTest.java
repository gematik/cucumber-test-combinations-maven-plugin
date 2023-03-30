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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import de.gematik.combine.model.CombineItem;
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

  public static final String ALL_WITH_PROFILE_SET_JSON = "allWithProfileSet.json";
  public static final String ONE_SINGLE_GROUP_AND_ONE_DOUBLE_GROUP = "oneSingleGroupAndOneDoubleGroup.json";
  public static final String TWO_WITH_AND_TWO_WITHOUT_GROUP = "twoWithAndTwoWithoutGroup.json";
  public static final String ONE_WITH_TWO_GROUPS = "oneWithTwoGroups.json";
  private final String baseInputPath = "./src/test/resources/input/groups/".replace("/",
      File.separator);

  private Pooler pooler = new Pooler();
  private PrepareItemsConfigBuilder confiBuilder;

  @BeforeEach
  void createBasicConfigBuilder() {
    confiBuilder = PrepareItemsConfig.builder()
        .excludedGroups(new ArrayList<>()).groups(new ArrayList<>());
  }

  @Test
  @SneakyThrows
  void returnAll() {
    PrepareItemsConfig config = confiBuilder
        .combineItemsFile(baseInputPath + "noProfileSet.json")
        .groups(List.of())
        .build();
    pooler.setConfig(config);

    List<CombineItem> res = pooler.pool();
    assertThat(res).hasSize(4);
  }

  private static Stream<Arguments> returnOnlyItemsHavingGroup() {
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
  @ParameterizedTest(name = "{0}")
  void returnOnlyItemsHavingGroup(List<String> groups, Map<String, Integer> sizeMap) {
    PrepareItemsConfig config = confiBuilder
        .combineItemsFile(baseInputPath + ALL_WITH_PROFILE_SET_JSON)
        .groups(groups)
        .build();
    pooler.setConfig(config);

    List<CombineItem> res = pooler.pool();
    assertThat(res).hasSize(groups.stream().map(sizeMap::get).reduce(0, (v1, v2) -> v1 + v2));
  }


  @Test
  @SneakyThrows
  void fillWithRandomProfileIfListSmallerThanPoolSize() {
    PrepareItemsConfig config = confiBuilder
        .combineItemsFile(baseInputPath + ONE_SINGLE_GROUP_AND_ONE_DOUBLE_GROUP)
        .groups(List.of("A"))
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
  void expectExceptionIfGroupHigherThanPoolSize() {
    PrepareItemsConfig config = confiBuilder
        .combineItemsFile(baseInputPath + ALL_WITH_PROFILE_SET_JSON)
        .groups(List.of("A", "B", "C"))
        .poolSize(2)
        .build();
    pooler.setConfig(config);

    assertThatThrownBy(() -> pooler.pool()).isInstanceOf(MojoExecutionException.class)
        .hasMessage("The given amount of groups is higher than the requested poolSize");
  }

  @Test
  @SneakyThrows
  void shouldUseItemWithoutGroupToFill() {
    PrepareItemsConfig config = confiBuilder
        .combineItemsFile(baseInputPath + TWO_WITH_AND_TWO_WITHOUT_GROUP)
        .groups(List.of("A", "B"))
        .poolSize(3)
        .build();
    pooler.setConfig(config);

    List<CombineItem> res = pooler.pool();
    assertThat(res).hasSize(3);
  }

  @Test
  @SneakyThrows
  void shouldCountTwoGroupsOfOneItemAsOnlyOneItemAndContinueFilling() {
    PrepareItemsConfig config = confiBuilder
        .combineItemsFile(baseInputPath + ONE_WITH_TWO_GROUPS)
        .groups(List.of("A"))
        .poolSize(2)
        .build();
    pooler.setConfig(config);

    List<CombineItem> res = pooler.pool();
    assertThat(res).hasSize(2);

  }

  @Test
  @SneakyThrows
  void shouldContinueFillingIfItemIsInBothGroups() {
    PrepareItemsConfig config = confiBuilder
        .combineItemsFile(baseInputPath + ONE_WITH_TWO_GROUPS)
        .groups(List.of("A", "B"))
        .poolSize(2)
        .build();
    pooler.setConfig(config);

    List<CombineItem> res = pooler.pool();
    assertThat(res).hasSize(2);
  }

  @Test
  @SneakyThrows
  void expectExceptionIfExcludeDoesNotMakeItPossibleToFulfillPoolSize() {
    PrepareItemsConfig config = confiBuilder
        .combineItemsFile(baseInputPath + TWO_WITH_AND_TWO_WITHOUT_GROUP)
        .groups(List.of("A"))
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
    PrepareItemsConfig config = confiBuilder
        .combineItemsFile(baseInputPath + TWO_WITH_AND_TWO_WITHOUT_GROUP)
        .groups(List.of("A"))
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
    PrepareItemsConfig config = confiBuilder
        .combineItemsFile(baseInputPath + TWO_WITH_AND_TWO_WITHOUT_GROUP)
        .excludedGroups(List.of("B"))
        .poolSize(0)
        .build();
    pooler.setConfig(config);

    List<CombineItem> res = pooler.pool();
    assertThat(res).hasSize(3);
  }
}
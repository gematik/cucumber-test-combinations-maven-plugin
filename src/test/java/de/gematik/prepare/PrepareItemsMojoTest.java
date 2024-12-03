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

package de.gematik.prepare;

import static de.gematik.prepare.PrepareItemsMojo.GENERATED_COMBINE_ITEMS_DIR;
import static de.gematik.prepare.PrepareItemsMojo.USED_GROUPS_PATH;
import static de.gematik.prepare.pooling.GroupMatchStrategyType.WILDCARD;
import static de.gematik.utils.Utils.getItemsToCombine;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.stream;
import static java.util.Comparator.comparing;
import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.jayway.jsonpath.JsonPath;
import de.gematik.combine.model.CombineItem;
import de.gematik.prepare.pooling.PoolGroup;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

class PrepareItemsMojoTest extends AbstractPrepareTest {

  public static final String ONLY_VALUE_JSON = "src/test/resources/input/inputOnlyValue.json";
  public static final String URL_VALUE_JSON = "src/test/resources/input/inputValueAndUrl.json";
  public static final String POOL_TEST = "src/test/resources/input/poolTest.json";

  @Test
  @SneakyThrows
  void shouldCreateNewCombineItemsFileAndGiveItToCombineMojo() {
    // arrange
    mojo.setCombineItemsFile(ONLY_VALUE_JSON);
    mojo.setItems(getItemsToCombine(new File(ONLY_VALUE_JSON), mojo, false));
    File outputDir = new File(GENERATED_COMBINE_ITEMS_DIR);
    FileUtils.deleteDirectory(new File(GENERATED_COMBINE_ITEMS_DIR));
    outputDir.mkdirs();
    long filesBefore =
        stream(requireNonNull(outputDir.listFiles()))
            .filter(e -> e.getName().endsWith(".json"))
            .count();
    // act
    mojo.run();
    // assert
    long filesAfter =
        stream(requireNonNull(outputDir.listFiles()))
            .filter(e -> e.getName().endsWith(".json"))
            .count();
    assertThat(filesBefore + 1).isEqualTo(filesAfter);
  }

  @Test
  @SneakyThrows
  void shouldUseUrlNotValue() {
    // arrange
    mojo.setCombineItemsFile(URL_VALUE_JSON);
    mojo.setItems(getItemsToCombine(new File(URL_VALUE_JSON), mojo, false));
    new File(GENERATED_COMBINE_ITEMS_DIR).mkdirs();
    // act
    mojo.run();
    // assert
    verify(apiRequester, times(3)).getApiResponse(startsWith("http://localhost:80"));
  }

  @Test
  @SneakyThrows
  void shouldUseValueBecauseUrlNotPresent() {
    // arrange
    mojo.setCombineItemsFile(ONLY_VALUE_JSON);
    mojo.setItems(getItemsToCombine(new File(ONLY_VALUE_JSON), mojo, false));
    new File(GENERATED_COMBINE_ITEMS_DIR).mkdirs();
    // act
    mojo.run();
    // assert
    verify(apiRequester, times(3)).getApiResponse(startsWith("API-"));
  }

  @Test
  @SneakyThrows
  void shouldNotThrowButDeleteNotReachableApi() {
    // arrange
    List<CombineItem> items = getItemsToCombine(new File(ONLY_VALUE_JSON), mojo, false);
    int amountBefore = items.size();
    mojo.setItems(items);
    mojo.setCombineItemsFile(ONLY_VALUE_JSON);
    mojo.setBreakOnFailedRequest(false);
    doThrow(new MojoExecutionException("Could not reach"))
        .when(apiRequester)
        .getApiResponse(items.get(1).getValue());
    // assert
    assertThatNoException().isThrownBy(mojo::run);
    String fileName = new File(ONLY_VALUE_JSON).getName();
    File generatedFile =
        stream(requireNonNull(new File(GENERATED_COMBINE_ITEMS_DIR).listFiles()))
            .filter(e -> e.getName().endsWith(fileName))
            .max(comparing(File::getName))
            .orElseThrow();
    List<CombineItem> itemsToCombine =
        getItemsToCombine(new File(generatedFile.getAbsolutePath()), mojo, false);
    assertThat(itemsToCombine).hasSize(amountBefore - 1);
  }

  static Stream<Arguments> getWrong() {
    return Stream.of(
        Arguments.of("property", List.of(new PropertyExpression(null, "exp")), List.of()),
        Arguments.of("expression", List.of(new PropertyExpression("prop", null)), List.of()),
        Arguments.of("tag", List.of(), List.of(new TagExpression(null, "exp"))),
        Arguments.of("expression", List.of(), List.of(new TagExpression("tag", null))));
  }

  @ParameterizedTest
  @SneakyThrows
  @MethodSource("getWrong")
  void shouldThrowExceptionIfValueIsMissingInExpression(
      String missingType, List<PropertyExpression> properties, List<TagExpression> tags) {
    // arrange

    mojo.setTagExpressions(tags);
    mojo.setPropertyExpressions(properties);
    // assert
    assertThatThrownBy(() -> mojo.checkExpressionSetCorrectly())
        .isInstanceOf(MojoExecutionException.class)
        .message()
        .startsWith("Erroneous configuration: missing " + missingType + " in ");
  }

  @Test
  @SneakyThrows
  void shouldThrowExceptionIfTagOrPropertyMismatch() {
    when(itemsCreator.getContextErrors()).thenReturn(List.of("Some Error"));
    mojo.setItems(List.of());
    mojo.setBreakOnContextError(true);
    assertThatThrownBy(() -> mojo.run())
        .isInstanceOf(MojoExecutionException.class)
        .message()
        .startsWith("Different tags or properties where found");
  }

  @Test
  @SneakyThrows
  @SuppressWarnings("java:S2699")
  void shouldNotThrowExceptionIfTagOrPropertyMismatch() {
    when(itemsCreator.getContextErrors()).thenReturn(List.of("Some Error"));
    mojo.setItems(List.of());
    mojo.setBreakOnContextError(false);
    mojo.setCombineItemsFile(ONLY_VALUE_JSON);
    mojo.run();
  }

  @Test
  @SneakyThrows
  void shouldCreateUsedGroupCorrectly() {
    // arrange
    mojo.setCombineItemsFile(POOL_TEST);
    mojo.setDefaultMatchStrategy(WILDCARD);
    mojo.setPropertyExpressions(List.of());
    mojo.setTagExpressions(List.of());
    mojo.setExcludedGroups(List.of("excluded"));
    mojo.setPoolGroupString("A");
    // act
    mojo.execute();
    // assert
    String jsonString = FileUtils.readFileToString(new File(USED_GROUPS_PATH), UTF_8);
    List<String> usedItems = JsonPath.read(jsonString, "$.usedItems");
    List<PoolGroup> poolGroups = JsonPath.read(jsonString, "$.poolGroups");
    List<String> usedGroups = JsonPath.read(jsonString, "$.usedGroups.A");
    Assertions.assertAll(
        () -> assertThat(usedItems).hasSize(2),
        () -> assertThat(poolGroups).hasSize(1),
        () -> assertThat(usedGroups).hasSize(2),
        () -> assertThat(usedGroups).containsExactlyInAnyOrder("API-1", "API-2"));
  }

  @Test
  @SneakyThrows
  void shouldAddRandomlySelectedGroupToPoolGroups() {
    // arrange
    mojo.setCombineItemsFile(POOL_TEST);
    mojo.setDefaultMatchStrategy(WILDCARD);
    mojo.setPropertyExpressions(List.of());
    mojo.setTagExpressions(List.of());
    mojo.setExcludedGroups(List.of("excluded"));
    mojo.setPoolGroupString("C");
    mojo.setPoolSize(2);
    // act
    mojo.execute();
    // assert
    String jsonString = FileUtils.readFileToString(new File(USED_GROUPS_PATH), UTF_8);
    System.out.println(jsonString);
    List<String> usedItems = JsonPath.read(jsonString, "$.usedItems");
    List<PoolGroup> poolGroups = JsonPath.read(jsonString, "$.poolGroups");
    Map<String, List<String>> usedGroups = JsonPath.read(jsonString, "$.usedGroups");
    Assertions.assertAll(
        () -> assertThat(usedItems).as("1").hasSize(3),
        () -> assertThat(poolGroups).as("2").hasSize(2),
        () -> assertThat(usedGroups).containsKey("C"),
        () -> assertThat(usedGroups.keySet()).hasSize(2));
  }
}

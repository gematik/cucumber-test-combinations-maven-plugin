/*
 * Copyright (c) 2022 gematik GmbH
 * 
 * Licensed under the Apache License, Version 2.0 (the License);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.gematik.prepare;

import static de.gematik.prepare.PrepareItemsMojo.GENERATED_COMBINE_ITEMS_DIR;
import static de.gematik.utils.Utils.getItemsToCombine;
import static java.util.Arrays.stream;
import static java.util.Comparator.comparing;
import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import de.gematik.combine.model.CombineItem;
import de.gematik.prepare.request.ApiRequester;
import java.io.File;
import java.util.List;
import java.util.stream.Stream;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PrepareItemsMojoTest {

  public static final String ONLY_VALUE_JSON = "src/test/resources/input/inputOnlyValue.json";
  @Mock
  ItemsCreator itemsCreator;

  @Mock
  ApiRequester apiRequester;

  PrepareItemsMojo mojo;

  Log log;

  @BeforeEach
  @SneakyThrows
  void setup() {
    mojo = spy(new PrepareItemsMojo(apiRequester));
    log = mock(Log.class);
    lenient().when(mojo.getLog()).thenReturn(log);
    mojo.setItemsCreator(itemsCreator);
    lenient().doReturn("{}").when(apiRequester).getApiResponse(any());
  }

  @Test
  @SneakyThrows
  void shouldCreateNewCombineItemsFileAndGiveItToCombineMojo() {
    // arrange
    mojo.setCombineItemsFile(ONLY_VALUE_JSON);
    mojo.setItems(getItemsToCombine(new File(ONLY_VALUE_JSON), mojo, false));
    File outputDir = new File(GENERATED_COMBINE_ITEMS_DIR);
    FileUtils.deleteDirectory(new File(GENERATED_COMBINE_ITEMS_DIR));
    outputDir.mkdirs();
    long filesBefore = stream(requireNonNull(outputDir.listFiles()))
        .filter(e -> e.getName().endsWith(".json")).count();
    // act
    mojo.run();
    // assert
    long filesAfter = stream(requireNonNull(outputDir.listFiles()))
        .filter(e -> e.getName().endsWith(".json")).count();
    assertThat(filesBefore + 1).isEqualTo(filesAfter);
  }


  @Test
  @SneakyThrows
  void shouldDeleteNotReachableApi() {
    // arrange
    List<CombineItem> items = getItemsToCombine(new File(ONLY_VALUE_JSON), mojo, false);
    int amountBefore = items.size();
    mojo.setItems(items);
    mojo.setCombineItemsFile(ONLY_VALUE_JSON);
    doThrow(new MojoExecutionException("Could not reach")).when(apiRequester)
        .getApiResponse(items.get(1).getValue());
    // act
    mojo.run();
    // assert
    String fileName = new File(ONLY_VALUE_JSON).getName();
    File generatedFile = stream(requireNonNull(new File(GENERATED_COMBINE_ITEMS_DIR).listFiles()))
        .filter(e -> e.getName().endsWith(fileName))
        .max(comparing(File::getName))
        .orElseThrow();
    List<CombineItem> itemsToCombine = getItemsToCombine(new File(generatedFile.getAbsolutePath()),
        mojo, false);
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
  void shouldThrowExceptionIfNoTagIsInProperty(String missingType,
      List<PropertyExpression> properties, List<TagExpression> tags) {
    // arrange

    mojo.setTagExpressions(tags);
    mojo.setPropertyExpressions(properties);
    // assert
    assertThatThrownBy(() -> mojo.checkExpressionSetCorrectly())
        .isInstanceOf(MojoExecutionException.class)
        .message().startsWith(
            "Erroneous configuration: missing " + missingType + " in ");
  }
}

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

package de.gematik.check;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.io.FileUtils.readFileToString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import de.gematik.combine.model.CombineItem;
import de.gematik.combine.model.CombineItem.CombineItemBuilder;
import de.gematik.utils.Utils;
import de.gematik.utils.request.ApiRequester;
import java.io.File;
import java.util.List;
import java.util.stream.Stream;
import lombok.SneakyThrows;
import org.apache.maven.plugin.MojoExecutionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CheckApiMojoTest {

  static final String FAIL = "fail";
  static final File CHECK_JSON = new File("./src/test/resources/responses/jsonCheckResponse.json");

  static final List<CombineItem> okItems =
      List.of(
          itemWith().value("UseExpressionOfItem").checkExpression("$.justATrueValue").build(),
          itemWith().value("UseDefaultExpression").build(),
          itemWith()
              .value("ComplexPath")
              .checkExpression("$.justAComplexPath.stillComplex.andFurther")
              .build(),
          itemWith().value("ListSizeCheck").checkExpression("$.justAList.size() <= 3").build(),
          itemWith()
              .value("UseItemInfo")
              .checkExpression(
                  "ITEM.getTags().contains(\"hasTag\") ? $.justATrueValue : $.justAFalseValue")
              .tags(List.of("hasTag"))
              .build());

  static final List<CombineItem> failItems =
      List.of(
          itemWith().value("falseButShouldBeTrue").checkExpression("$.justAFalseValue").build(),
          itemWith().value("invalidExpression").checkExpression("invalid expression").build(),
          itemWith().value("defaultExpression").build(),
          itemWith()
              .value("UseItemInfo")
              .checkExpression(
                  "ITEM.getTags().contains(\"hasTags\") ? $.justATrueValue : $.justAFalseValue")
              .tags(List.of("hasWrongTag"))
              .build());
  @Mock ApiRequester apiRequester;

  static CheckMojo mojo;

  static CombineItemBuilder itemWith() {
    return CombineItem.builder().url("egal");
  }

  static void makeMockReturnItems(MockedStatic<Utils> mocked, List<CombineItem> value) {
    mocked.when(() -> Utils.getItemsToCombine(any(), any(), anyBoolean())).thenReturn(value);
  }

  static Stream<Arguments> runSuccessfully() {
    return okItems.stream().map(List::of).map(Arguments::of);
  }

  static Stream<Arguments> runFailOnRequest() {
    return okItems.stream()
        .map(CombineItem::toBuilder)
        .peek(builder -> builder.url(FAIL))
        .map(CombineItemBuilder::build)
        .map(List::of)
        .map(Arguments::of);
  }

  static Stream<Arguments> runFailOnContext() {
    return failItems.stream().map(List::of).map(Arguments::of);
  }

  static Stream<Arguments> runFailOnRequestDontBreak() {
    return runFailOnRequest();
  }

  static Stream<Arguments> runFailOnContextDontBreak() {
    return runFailOnContext();
  }

  @BeforeEach
  @SneakyThrows
  void setupMojoAndRequester() {
    mojo = new CheckMojo(apiRequester);
    mojo.setCombineItemsFile("notImportant");
    mojo.setDefaultCheckExpressions("$.justATrueValue");
    mojo.setBreakOnFailedRequest(true);
    mojo.setBreakOnContextError(true);
    lenient()
        .when(apiRequester.getApiResponse(any()))
        .thenReturn(readFileToString(CHECK_JSON, UTF_8));
    lenient()
        .when(apiRequester.getApiResponse(startsWith(FAIL)))
        .thenThrow(new MojoExecutionException("request failed"));
  }

  @ParameterizedTest
  @MethodSource
  @SneakyThrows
  void runSuccessfully(List<CombineItem> items) {
    // arrange
    try (MockedStatic<Utils> mocked = mockStatic(Utils.class)) {
      makeMockReturnItems(mocked, items);
      // assert
      assertThatNoException().isThrownBy(mojo::execute);
      assertThat(mojo.getErrors()).isEmpty();
      assertThat(mojo.getApiErrors()).isEmpty();
    }
  }

  @ParameterizedTest
  @MethodSource
  @SneakyThrows
  void runFailOnRequest(List<CombineItem> items) {
    // arrange
    try (MockedStatic<Utils> mocked = mockStatic(Utils.class)) {
      makeMockReturnItems(mocked, items);
      // assert
      assertThatThrownBy(mojo::execute).isInstanceOf(MojoExecutionException.class);
      assertThat(mojo.getApiErrors()).hasSize(1);
      assertThat(mojo.getErrors()).isEmpty();
    }
  }

  @ParameterizedTest
  @MethodSource
  @SneakyThrows
  void runFailOnContext(List<CombineItem> items) {
    // arrange
    try (MockedStatic<Utils> mocked = mockStatic(Utils.class)) {
      makeMockReturnItems(mocked, items);
      mojo.setDefaultCheckExpressions("$.justAFalseValue");
      // assert
      assertThatThrownBy(mojo::execute).isInstanceOf(MojoExecutionException.class);
      assertThat(mojo.getApiErrors()).isEmpty();
      assertThat(mojo.getErrors()).hasSize(1);
    }
  }

  @ParameterizedTest
  @MethodSource
  @SneakyThrows
  void runFailOnContextDontBreak(List<CombineItem> items) {
    // arrange
    try (MockedStatic<Utils> mocked = mockStatic(Utils.class)) {
      makeMockReturnItems(mocked, items);
      mojo.setBreakOnContextError(false);
      mojo.setDefaultCheckExpressions("$.justAFalseValue");
      // assert
      assertThatNoException().isThrownBy(mojo::execute);
      assertThat(mojo.getApiErrors()).isEmpty();
      assertThat(mojo.getErrors()).hasSize(1);
    }
  }

  @ParameterizedTest
  @MethodSource
  @SneakyThrows
  void runFailOnRequestDontBreak(List<CombineItem> items) {
    // arrange
    try (MockedStatic<Utils> mocked = mockStatic(Utils.class)) {
      makeMockReturnItems(mocked, items);
      mojo.setBreakOnFailedRequest(false);
      // assert
      assertThatNoException().isThrownBy(mojo::execute);
      assertThat(mojo.getApiErrors()).hasSize(1);
      assertThat(mojo.getErrors()).isEmpty();
    }
  }

  @Test
  @SneakyThrows
  void getFirstExpression() {
    // arrange
    try (MockedStatic<Utils> mocked = mockStatic(Utils.class)) {
      makeMockReturnItems(mocked, List.of(new CombineItem()));
      mojo.getCheckExpressions()
          .add(CheckExpression.builder().id("someId").expression("$.justATrueValue").build());
      // act
      mojo.execute();
      // assert
      assertThat(mojo.getApiErrors()).isEmpty();
      assertThat(mojo.getErrors()).isEmpty();
    }
  }

  @Test
  @SneakyThrows
  void noExpression() {
    // arrange
    try (MockedStatic<Utils> mocked = mockStatic(Utils.class)) {
      makeMockReturnItems(mocked, List.of(new CombineItem()));
      mojo.setDefaultCheckExpressions(null);
      // assert
      assertThatThrownBy(mojo::execute).isInstanceOf(MojoExecutionException.class);
      assertThat(mojo.getErrors()).hasSize(1);
    }
  }

  @Test
  @SneakyThrows
  void notAbleParseResponse() {
    // arrange
    try (MockedStatic<Utils> mocked = mockStatic(Utils.class)) {
      makeMockReturnItems(mocked, List.of(CombineItem.builder().tags(List.of("myTag")).build()));
      when(apiRequester.getApiResponse(any())).thenReturn("Not json parsable");
      mojo.setDefaultCheckExpressions("ITEM.hasTag(\"myTag\")");
      // assert
      assertThatThrownBy(mojo::execute).isInstanceOf(MojoExecutionException.class);
      assertThat(mojo.getApiErrors()).isEmpty();
      assertThat(mojo.getErrors()).hasSize(1);
    }
  }
}

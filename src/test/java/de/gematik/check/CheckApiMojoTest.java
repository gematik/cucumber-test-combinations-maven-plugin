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

package de.gematik.check;


import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.io.FileUtils.readFileToString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import de.gematik.combine.model.CombineItem;
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

  @Mock
  ApiRequester apiRequester;

  @BeforeEach
  @SneakyThrows
  void setupApiRequester() {
    lenient().when(apiRequester.getApiResponse(any()))
        .thenReturn(
            readFileToString(new File("./src/test/resources/responses/jsonCheckResponse.json"),
                UTF_8));

  }

  public static Stream<Arguments> runSuccessfully() {
    return Stream.of(
        arguments(List.of(
            CombineItem.builder().value("UseExpressionOfItem").checkExpression("$.justATrueValue")
                .build())),
        arguments(List.of(CombineItem.builder().value("UseDefaultExpression").build())),
        arguments(List.of(CombineItem.builder().value("ComplexPath")
            .checkExpression("$.justAComplexPath.stillComplex.andFurther").build())),
        arguments(List.of(
            CombineItem.builder().value("ListSizeCheck").checkExpression("$.justAList.size() <= 3")
                .build())),
        arguments(List.of(CombineItem.builder().tags(List.of("hasTag")).value("UseItemInfo")
            .checkExpression(
                "ITEM.getTags().contains(\"hasTag\") ? $.justATrueValue : $.justAFalseValue")
            .build()))

    );
  }

  @ParameterizedTest
  @MethodSource
  @SneakyThrows
  void runSuccessfully(List<CombineItem> items) {
    try (MockedStatic<Utils> mocked = mockStatic(Utils.class)) {
      mocked.when(() -> Utils.getItemsToCombine(any(), any(), anyBoolean())).thenReturn(items);
      CheckMojo mojo = new CheckMojo(apiRequester);
      mojo.setCombineItemsFile("notImportant");
      mojo.setDefaultCheckExpressions("$.justATrueValue");
      mojo.execute();
      assertThat(mojo.getErrors()).isEmpty();
    }
  }

  public static Stream<Arguments> runFail() {
    return Stream.of(
        arguments(List.of(
            CombineItem.builder().value("falseButShouldBeTrue").checkExpression("$.justAFalseValue")
                .build())),
        arguments(List.of(
            CombineItem.builder().value("invalidExpression").checkExpression("invalid expression")
                .build())),
        arguments(List.of(CombineItem.builder().value("defaultExpression").build())),
        arguments(List.of(CombineItem.builder().tags(List.of("hasWrongTag")).value("UseItemInfo")
            .checkExpression(
                "ITEM.getTags().contains(\"hasTag\") ? $.justATrueValue : $.justAFalseValue")
            .build()))
    );
  }

  @ParameterizedTest
  @MethodSource
  @SneakyThrows
  void runFail(List<CombineItem> items) {
    try (MockedStatic<Utils> mocked = mockStatic(Utils.class)) {
      mocked.when(() -> Utils.getItemsToCombine(any(), any(), anyBoolean())).thenReturn(items);
      CheckMojo mojo = new CheckMojo(apiRequester);
      mojo.setCombineItemsFile("notImportant");
      mojo.setDefaultCheckExpressions("$.justAFalseValue");
      assertThatThrownBy(mojo::execute).isInstanceOf(MojoExecutionException.class);
      assertThat(mojo.getErrors()).hasSize(1);
    }
  }

  @Test
  @SneakyThrows
  void getFirstExpression() {
    try (MockedStatic<Utils> mocked = mockStatic(Utils.class)) {
      mocked.when(() -> Utils.getItemsToCombine(any(), any(), anyBoolean()))
          .thenReturn(List.of(CombineItem.builder().build()));
      CheckMojo mojo = new CheckMojo(apiRequester);
      mojo.setCombineItemsFile("notImportant");
      mojo.getCheckExpressions()
          .add(CheckExpression.builder().id("someId").expression("$.justATrueValue").build());
      mojo.execute();
      assertThat(mojo.getErrors()).isEmpty();
    }
  }

  @Test
  @SneakyThrows
  void noExpression() {
    try (MockedStatic<Utils> mocked = mockStatic(Utils.class)) {
      mocked.when(() -> Utils.getItemsToCombine(any(), any(), anyBoolean()))
          .thenReturn(List.of(CombineItem.builder().build()));
      CheckMojo mojo = new CheckMojo(apiRequester);
      mojo.setCombineItemsFile("notImportant");
      assertThatThrownBy(mojo::execute).isInstanceOf(MojoExecutionException.class);
      assertThat(mojo.getErrors()).hasSize(1);
    }
  }

  @Test
  @SneakyThrows
  void notAbleParseResponse() {
    try (MockedStatic<Utils> mocked = mockStatic(Utils.class)) {
      mocked.when(() -> Utils.getItemsToCombine(any(), any(), anyBoolean()))
          .thenReturn(List.of(CombineItem.builder().tags(List.of("myTag")).build()));
      when(apiRequester.getApiResponse(any())).thenReturn("Not json parsable");
      CheckMojo mojo = new CheckMojo(apiRequester);
      mojo.setDefaultCheckExpressions("ITEM.hasTag(\"myTag\")");
      mojo.setCombineItemsFile("notImportant");
      assertThatThrownBy(mojo::execute).isInstanceOf(MojoExecutionException.class);
      assertThat(mojo.getErrors()).hasSize(1);
    }
  }
}
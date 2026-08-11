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

package de.gematik.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Answers.RETURNS_SELF;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.gematik.utils.request.ApiRequester;
import de.gematik.utils.request.RequesterClient;
import de.gematik.utils.request.StatusCodes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import lombok.SneakyThrows;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.apache.maven.plugin.MojoExecutionException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ApiRequesterTest {

  @Mock(answer = RETURNS_SELF)
  RequesterClient requesterClient;

  @ParameterizedTest
  @MethodSource("getFamiliesWithInvalidCodes")
  @SneakyThrows
  void shouldReturnErrorIfResponseNotInNamedFamily(String families, List<Integer> responseCodes) {
    // arrange
    Response res = mock(Response.class);
    when(requesterClient.newCall(any())).thenReturn(res);

    // act
    ApiRequester apiRequester = new ApiRequester(requesterClient);
    apiRequester.setAllowedResponses(families, "");
    responseCodes.forEach(
        responseCode -> {
          when(res.code()).thenReturn(responseCode);
          // assert
          assertThrows(
              MojoExecutionException.class, () -> apiRequester.getApiResponse("http://someUrl"));
        });
  }

  private static Stream<Arguments> getFamiliesWithInvalidCodes() {
    List<Arguments> args = new ArrayList<>();
    for (List<StatusCodes> codes : generateCombinations(Arrays.asList(StatusCodes.values()))) {
      List<Integer> invalidIds =
          IntStream.range(100, 600)
              .filter(i -> codes.stream().noneMatch(statusCode -> statusCode.isValid(i)))
              .boxed()
              .toList();
      if (!invalidIds.isEmpty()) {
        args.add(
            Arguments.of(
                codes.stream().map(Enum::name).collect(Collectors.joining(",")), invalidIds));
      }
    }
    return args.stream();
  }

  private static List<List<StatusCodes>> generateCombinations(List<StatusCodes> elements) {
    List<List<StatusCodes>> result = new ArrayList<>();
    generateCombinationsHelper(elements, 0, new ArrayList<>(), result);
    return result.stream().filter(statusCode -> !statusCode.isEmpty()).toList();
  }

  private static void generateCombinationsHelper(
      List<StatusCodes> elements,
      int currentIndex,
      List<StatusCodes> currentCombination,
      List<List<StatusCodes>> result) {
    if (currentIndex == elements.size()) {
      result.add(new ArrayList<>(currentCombination));
      return;
    }
    currentCombination.add(elements.get(currentIndex));
    generateCombinationsHelper(elements, currentIndex + 1, currentCombination, result);
    currentCombination.removeLast();
    generateCombinationsHelper(elements, currentIndex + 1, currentCombination, result);
  }

  @Test
  @SneakyThrows
  void shouldAcceptDefinedStatusCodes() {
    // arrange
    String responseString = "Some Test Response";
    ResponseBody body = mock(ResponseBody.class);
    Response response = mock(Response.class);
    ApiRequester apiRequester = new ApiRequester(requesterClient);

    Random random = new Random();
    List<Integer> randomAllowedCodes =
        IntStream.range(0, 50).mapToObj(i -> random.nextInt(499) + 100).toList();
    apiRequester.setAllowedResponses(
        "", randomAllowedCodes.stream().map(String::valueOf).collect(Collectors.joining(",")));

    when(requesterClient.newCall(any())).thenReturn(response);
    when(response.body()).thenReturn(body);
    when(body.string()).thenReturn(responseString);

    // act
    for (int i = 100; i < 600; ++i) {
      when(response.code()).thenReturn(i);
      if (randomAllowedCodes.contains(i)) {
        assertThat(apiRequester.getApiResponse("http://someUrl")).isEqualTo(responseString);
      } else {
        // assert
        assertThatThrownBy(() -> apiRequester.getApiResponse("http://someUrl"))
            .isInstanceOf(MojoExecutionException.class)
            .hasMessage(
                "Response code was "
                    + i
                    + " and not defined in valid response codes "
                    + randomAllowedCodes);
      }
    }
  }

  @Test
  void shouldThrowExceptionIfStringIsProvided() {
    assertThatThrownBy(() -> new ApiRequester(requesterClient).setAllowedResponses("", "asdf"))
        .isInstanceOf(MojoExecutionException.class)
        .hasMessage("Invalid status code was provided in [asdf]");
  }

  @Test
  void shouldThrowExceptionIfCodeToLowIsProvided() {
    for (int i = -5; i <= 99; i++) {
      int code = i;
      assertThatThrownBy(
              () -> new ApiRequester(requesterClient).setAllowedResponses("", String.valueOf(code)))
          .isInstanceOf(MojoExecutionException.class)
          .hasMessage("Codes could only have a range of 100 - 599");
    }
  }

  @Test
  void shouldThrowExceptionIfCodeToHighIsProvided() {
    for (int i = 600; i <= 650; i++) {
      int code = i;
      assertThatThrownBy(
              () -> new ApiRequester(requesterClient).setAllowedResponses("", String.valueOf(code)))
          .isInstanceOf(MojoExecutionException.class)
          .hasMessage("Codes could only have a range of 100 - 599");
    }
  }

  @Test
  void shouldThrowExceptionIfUnknownResponseFamilyNamed() {
    // arrange
    ApiRequester apiRequester = new ApiRequester(requesterClient);
    // act
    assertThatThrownBy(() -> apiRequester.setAllowedResponses("unknown", ""))
        // assert
        .isInstanceOf(MojoExecutionException.class)
        .hasMessage(
            "Unknown status code family found in \"unknown\" allowed values: [INFO, SUCCESS, REDIRECTION, CLIENT_ERROR, SERVER_ERROR, ALL]");
  }
}

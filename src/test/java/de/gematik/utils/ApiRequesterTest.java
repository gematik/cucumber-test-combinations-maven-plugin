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

package de.gematik.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.gematik.utils.request.ApiRequester;
import de.gematik.utils.request.ApiRequester.StatusCodes;
import lombok.SneakyThrows;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.apache.maven.plugin.MojoExecutionException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

class ApiRequesterTest {

  @Test
  void shouldThrowExceptionIfOnlyOneProxyParameterIsMissing() {
    ApiRequester apiRequester = new ApiRequester();
    apiRequester.setupProxy("host", null);
    assertThrows(MojoExecutionException.class,
        () -> apiRequester.getApiResponse("anyUrl"));
    apiRequester.setupProxy(null, 420);
    assertThrows(MojoExecutionException.class,
        () -> apiRequester.getApiResponse("anyUrl"));
  }

  @ParameterizedTest
  @MethodSource("getWrongSSLParams")
  void shouldThrowExceptionIfOnlyOneSSLParameterIsMissing(String trust, String trustPw,
                                                          String client, String clientPw) {
    ApiRequester apiRequester = new ApiRequester();
    apiRequester.setupTls(trust, trustPw, client, clientPw);
    assertThrows(MojoExecutionException.class,
        () -> apiRequester.getApiResponse("anyUrl"));
  }

  @ParameterizedTest
  @MethodSource("getFamiliesWithInvalidCodes")
  @SneakyThrows
  void shouldReturnErrorIfResponseNotInNamedFamily(String families, List<Integer> codes) {
    // arrange
    ApiRequester apiRequester = new ApiRequester();
    OkHttpClient client = mock(OkHttpClient.class);
    Call call = mock(Call.class);
    Response res = mock(Response.class);
    apiRequester.setClient(client);
    apiRequester.setAllowedResponses(families, "");
    when(client.newCall(any())).thenReturn(call);
    when(call.execute()).thenReturn(res);
    codes.forEach(i -> {
      when(res.code()).thenReturn(i);
      // act
      MojoExecutionException ex = assertThrows(MojoExecutionException.class, ()
          // assert
          -> apiRequester.getApiResponse("http://someUrl"));
    });
  }

  @Test
  @SneakyThrows
  void shouldAcceptDefinedStatusCodes() {
    // arrange
    String responseString = "Some Test Response";

    OkHttpClient client = mock(OkHttpClient.class);
    Call call = mock(Call.class);
    ResponseBody body = mock(ResponseBody.class);
    Response res = mock(Response.class);

    Random random = new Random();
    ApiRequester apiRequester = new ApiRequester();
    List<Integer> randomValues = IntStream.range(0, 50).mapToObj(i -> random.nextInt(499) + 100).collect(Collectors.toList());
    apiRequester.setAllowedResponses("", randomValues.stream().map(String::valueOf).collect(Collectors.joining(",")));
    apiRequester.setClient(client);


    when(client.newCall(any())).thenReturn(call);
    when(call.execute()).thenReturn(res);
    when(res.body()).thenReturn(body);
    when(body.string()).thenReturn(responseString);

    // act
    for (int i = 100; i < 600; ++i) {
      when(res.code()).thenReturn(i);
      if (randomValues.contains(i)) {
        assertThat(apiRequester.getApiResponse("http://someUrl")).isEqualTo(responseString);
      } else {
        assertThatThrownBy(() -> apiRequester.getApiResponse("http://someUrl"))
            .isInstanceOf(MojoExecutionException.class)
            .hasMessage("Response code was " + i + " and not defined in valid response codes " + randomValues);
      }
    }
    // assert
  }

  @Test
  @SneakyThrows
  void shouldThrowExceptionIfStringIsProvided() {
    assertThatThrownBy(() -> new ApiRequester().setAllowedResponses("", "asdf"))
        .isInstanceOf(MojoExecutionException.class)
        .hasMessage("Invalid status code was provided in [asdf]");
  }

  @Test
  @SneakyThrows
  void shouldThrowExceptionIfCodeToLowIsProvided() {
    for (int i = -5; i <= 99; i++) {
      int finalI = i;
      assertThatThrownBy(() -> new ApiRequester().setAllowedResponses("", String.valueOf(finalI)))
          .isInstanceOf(MojoExecutionException.class)
          .hasMessage("Codes could only have a range of 100 - 599");
    }
  }

  @Test
  @SneakyThrows
  void shouldThrowExceptionIfCodeToHighIsProvided() {
    for (int i = 600; i <= 650; i++) {
      int finalI = i;
      assertThatThrownBy(() -> new ApiRequester().setAllowedResponses("", String.valueOf(finalI)))
          .isInstanceOf(MojoExecutionException.class)
          .hasMessage("Codes could only have a range of 100 - 599");
    }
  }

  @Test
  @SneakyThrows
  void shouldThrowExceptionIfNonKnownResponseFamilyNamed() {
    // arrange
    ApiRequester apiRequester = new ApiRequester();
    // act
    assertThatThrownBy(() -> apiRequester.setAllowedResponses("unknown", ""))
        // assert
        .isInstanceOf(MojoExecutionException.class)
        .hasMessage("Unknown status code family found in \"unknown\" allowed values: [INFO, SUCCESS, REDIRECTION, CLIENT_ERROR, SERVER_ERROR, ALL]");
  }

  private static Stream<Arguments> getFamiliesWithInvalidCodes() {
    List<Arguments> args = new ArrayList<>();
    for (List<StatusCodes> codes : generateCombinations(Arrays.asList(StatusCodes.values()))) {
      List<Integer> invalidIds = IntStream.range(100, 600)
          .filter(i -> codes.stream().noneMatch(status -> status.isValid(i)))
          .boxed()
          .collect(Collectors.toList());
      if (invalidIds.size() > 0) {
        args.add(Arguments.of(codes.stream().map(Enum::name).collect(Collectors.joining(",")), invalidIds));
      }
    }
    return args.stream();
  }

  private static Stream<Arguments> getWrongSSLParams() {
    List<Arguments> arguments = new ArrayList<>();
    for (int i = 1; i < 15; i++) {
      char[] charArray = padWithLeadingZeros(Integer.toBinaryString(i)).toCharArray();
      String[] args = new String[4];
      for (int j = 0; j < charArray.length; j++) {
        if (charArray[j] == '1') {
          args[j] = "something";
        }
      }
      arguments.add(Arguments.of(args[0], args[1], args[2], args[3]));
    }
    return arguments.stream();
  }

  private static String padWithLeadingZeros(String input) {
    assert input.length() <= 4 : "must be shorter than 5";
    return input.length() == 4 ? input : padWithLeadingZeros("0" + input);
  }


  public static List<List<StatusCodes>> generateCombinations(List<StatusCodes> elements) {
    List<List<StatusCodes>> result = new ArrayList<>();
    generateCombinationsHelper(elements, 0, new ArrayList<>(), result);
    return result.stream().filter(s -> !s.isEmpty()).collect(Collectors.toList());
  }

  private static void generateCombinationsHelper(List<StatusCodes> elements, int currentIndex, List<StatusCodes> currentCombination, List<List<StatusCodes>> result) {
    if (currentIndex == elements.size()) {
      result.add(new ArrayList<>(currentCombination));
      return;
    }
    currentCombination.add(elements.get(currentIndex));
    generateCombinationsHelper(elements, currentIndex + 1, currentCombination, result);
    currentCombination.remove(currentCombination.size() - 1);
    generateCombinationsHelper(elements, currentIndex + 1, currentCombination, result);
  }
}
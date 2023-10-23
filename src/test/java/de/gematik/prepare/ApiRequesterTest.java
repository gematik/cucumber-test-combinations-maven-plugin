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

import static org.junit.jupiter.api.Assertions.assertThrows;

import de.gematik.utils.request.ApiRequester;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.apache.maven.plugin.MojoExecutionException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

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
}
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

import static org.junit.jupiter.api.Assertions.assertThrows;

import de.gematik.prepare.request.ApiRequester;
import java.util.stream.Stream;
import org.apache.maven.plugin.MojoExecutionException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ApiRequesterTest {

  @ParameterizedTest
  @MethodSource("getWrongInput")
  void shouldThrowExceptionIfOnlyOneParameterIsMissing(String trust, String trustPw, String client,
      String clientPw) {
    ApiRequester apiRequester = new ApiRequester();
    assertThrows(MojoExecutionException.class,
        () -> apiRequester.setupTls(trust, trustPw, client, clientPw));
  }

  private static Stream<Arguments> getWrongInput() {
    return Stream.of(
        Arguments.of(null, "something", "something", "something"),
        Arguments.of("something", null, "something", "something"),
        Arguments.of("something", "something", null, "something"),
        Arguments.of("something", "something", "something", null),
        Arguments.of(null, null, "something", "something"),
        Arguments.of(null, null, "something", "something"),
        Arguments.of(null, "something", null, "something"),
        Arguments.of(null, "something", "something", null),
        Arguments.of("something", null, null, "something"),
        Arguments.of("something", null, "something", null),
        Arguments.of("something", "something", null, null),
        Arguments.of(null, null, null, "something"),
        Arguments.of(null, null, "something", null),
        Arguments.of(null, "something", null, null),
        Arguments.of("something", null, null, null)
    );
  }
}

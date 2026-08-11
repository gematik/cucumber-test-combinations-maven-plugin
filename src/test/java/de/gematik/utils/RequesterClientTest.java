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

import static de.gematik.utils.MockPluginLog.withMockedPluginLog;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;

import de.gematik.utils.request.RequesterClient;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import lombok.SneakyThrows;
import okhttp3.Request;
import org.apache.maven.plugin.MojoExecutionException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class RequesterClientTest {

  @Test
  void shouldThrowExceptionIfOnlyOneProxyParameterIsMissing() {
    assertThatThrownBy(() -> new RequesterClient().proxy("host", null).build())
        .isInstanceOf(MojoExecutionException.class)
        .hasMessage("You tried to set a proxy but one parameter is missing");
    assertThatThrownBy(() -> new RequesterClient().proxy(null, 420).build())
        .isInstanceOf(MojoExecutionException.class)
        .hasMessage("You tried to set a proxy but one parameter is missing");
  }

  @Test
  @SneakyThrows
  void proxyIsSet() {
    RequesterClient requesterClient = new RequesterClient().proxy("proxyhost", 420).build();
    Request request = new Request.Builder().url("http://gematik.de").build();

    //noinspection resource
    assertThatThrownBy(() -> requesterClient.newCall(request))
        .isInstanceOf(UnknownHostException.class)
        .hasMessage("proxyhost");
  }

  @Test
  @SneakyThrows
  void proxyIgnoredForNoProxyHost() {
    RequesterClient requesterClient =
        new RequesterClient().proxy("proxyhost", 420).noProxyHosts("localhost").build();
    Request request = new Request.Builder().url("http://localhost").build();

    //noinspection resource
    assertThatThrownBy(() -> requesterClient.newCall(request))
        .isInstanceOf(ConnectException.class)
        .hasMessage("Failed to connect to localhost/127.0.0.1:80");
  }

  @Test
  @SneakyThrows
  void proxyIgnoredForSeveralNoProxyHosts() {
    RequesterClient requesterClient =
        withMockedPluginLog(
            () ->
                new RequesterClient()
                    .proxy("proxyhost", 420)
                    .noProxyHosts("localhost, http://localhost,https://localhost")
                    .build());
    Request request = new Request.Builder().url("http://localhost").build();

    verify(MockPluginLog.getMock())
        .info(
            "Using proxy: proxyhost:420 (except for: [localhost,  http://localhost, https://localhost])");
    //noinspection resource
    assertThatThrownBy(() -> requesterClient.newCall(request))
        .isInstanceOf(ConnectException.class)
        .hasMessage("Failed to connect to localhost/127.0.0.1:80");
  }

  @ParameterizedTest
  @MethodSource("getWrongSSLParams")
  void shouldThrowExceptionIfOnlyOneSSLParameterIsMissing(
      String trust, String trustPw, String client, String clientPw) {
    assertThatThrownBy(() -> new RequesterClient().tls(trust, trustPw, client, clientPw).build())
        .isInstanceOf(MojoExecutionException.class)
        .hasMessage("You tried to set an mTLS context but at least one parameter is missing");
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

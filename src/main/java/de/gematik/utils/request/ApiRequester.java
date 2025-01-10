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

package de.gematik.utils.request;

import static de.gematik.utils.Utils.getLog;
import static de.gematik.utils.request.SSLContextFactory.getX509TrustManager;
import static okhttp3.ConnectionSpec.MODERN_TLS;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.net.ssl.SSLContext;
import lombok.Setter;
import lombok.SneakyThrows;
import okhttp3.OkHttpClient;
import okhttp3.OkHttpClient.Builder;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.MojoExecutionException;

public class ApiRequester {

  private String trustStorePath;
  private String clientCertPath;
  private String trustStorePassword;
  private String clientCertPassword;
  private String proxyHost;
  private Integer proxyPort;
  @Setter private OkHttpClient client;
  private List<StatusCodes> allowedFam;
  private List<Integer> allowedCodes;

  public String getApiResponse(String url) throws MojoExecutionException {
    if (client == null) {
      setupAndCreateClient();
    }
    Request request = new Request.Builder().url(url).build();
    try (Response resp = client.newCall(request).execute(); ) {
      validateCode(resp.code());
      return resp.body().string();
    } catch (IOException e) {
      throw new MojoExecutionException(
          "API did not respond -> " + url + " cause: " + e.getMessage());
    }
  }

  private void validateCode(int code) throws MojoExecutionException {
    if (allowedCodes == null) {
      if (allowedFam.stream().noneMatch(sc -> sc.isValid(code))) {
        throw new MojoExecutionException(
            "Response code was " + code + " and not in allowed response families" + allowedFam);
      }
    } else {
      if (!allowedCodes.contains(code)) {
        throw new MojoExecutionException(
            "Response code was "
                + code
                + " and not defined in valid response codes "
                + allowedCodes);
      }
    }
  }

  @SneakyThrows
  private void setupAndCreateClient() {
    Builder builder = new Builder();
    checkAndConfigureProxy(builder);
    checkAndConfigureMtls(builder);
    client = builder.build();
  }

  @SuppressWarnings("java:S5527")
  private void checkAndConfigureMtls(Builder builder) throws MojoExecutionException {
    if (sslParameters().noneMatch(Objects::isNull)) {
      SSLContext sslContext =
          SSLContextFactory.createAndGetSSLContext(
              clientCertPath, trustStorePath, clientCertPassword, trustStorePassword);
      builder
          .hostnameVerifier((hostname, session) -> true)
          .connectionSpecs(List.of(MODERN_TLS))
          .sslSocketFactory(
              sslContext.getSocketFactory(),
              getX509TrustManager(trustStorePath, trustStorePassword))
          .build();
      getLog().info("Using mTLS");
    } else if (sslParameters().allMatch(Objects::isNull)) {
      getLog().warn("Using no TLS");
    } else {
      Map<String, String> variableMap = new HashMap<>();
      variableMap.put("clientCertPath", clientCertPath);
      variableMap.put("trustStorePath", trustStorePath);
      variableMap.put("clientCertPassword", clientCertPassword);
      variableMap.put("trustStorePassword", trustStorePassword);
      for (Map.Entry<String, String> entry : variableMap.entrySet()) {
        if(entry.getValue() == null || entry.getValue().isEmpty()){
          getLog().warn(entry.getKey() + " is missing.");
        }else{
          getLog().info(entry.getKey() + " is set.");
        }
      }
      throw new MojoExecutionException(
          "You tried to set an mTLS context but at least one parameter is missing");
    }
  }

  private void checkAndConfigureProxy(Builder builder) throws MojoExecutionException {
    if (proxyParameters().noneMatch(Objects::isNull)) {
      builder.proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort)));
      getLog().info("Using proxy: " + proxyHost + ":" + proxyPort);
    } else if (proxyParameters().allMatch(Objects::isNull)) {
      getLog().warn("Using no proxy");
    } else {
      throw new MojoExecutionException("You tried to set a proxy but one parameter is missing");
    }
  }

  public void setAllowedResponses(String fam, String codes) throws MojoExecutionException {
    if (StringUtils.isNotBlank(codes)) {
      try {
        List<Integer> namedCodes =
            Arrays.stream(codes.split(",")).map(Integer::valueOf).collect(Collectors.toList());
        if (namedCodes.stream().anyMatch(i -> i >= 600 || i < 100)) {
          throw new MojoExecutionException("Codes could only have a range of 100 - 599");
        }
        this.allowedCodes = namedCodes;
      } catch (NumberFormatException ex) {
        throw new MojoExecutionException("Invalid status code was provided in [" + codes + "]");
      }
    } else {
      try {
        allowedFam =
            Arrays.asList(fam.split(",")).stream()
                .map(StatusCodes::valueOf)
                .collect(Collectors.toList());
      } catch (IllegalArgumentException ex) {
        throw new MojoExecutionException(
            "Unknown status code family found in \""
                + fam
                + "\" allowed values: "
                + Arrays.toString(StatusCodes.values()));
      }
    }
  }

  public void setupProxy(String proxyHost, Integer proxyPort) {
    this.proxyHost = proxyHost;
    this.proxyPort = proxyPort;
  }

  public void setupTls(
      String trustStorePath,
      String trustStorePassword,
      String clientCertPath,
      String clientCertPassword) {
    this.trustStorePath = trustStorePath;
    this.trustStorePassword = trustStorePassword;
    this.clientCertPath = clientCertPath;
    this.clientCertPassword = clientCertPassword;
  }

  private Stream<Object> proxyParameters() {
    return Stream.of(proxyHost, proxyPort);
  }

  private Stream<Object> sslParameters() {
    return Stream.of(trustStorePath, trustStorePassword, clientCertPath, clientCertPassword);
  }

  public enum StatusCodes {
    INFO(100, 199),
    SUCCESS(200, 299),
    REDIRECTION(300, 399),
    CLIENT_ERROR(400, 499),
    SERVER_ERROR(500, 599),
    ALL(100, 599);

    final int from;
    final int to;

    StatusCodes(int from, int to) {
      this.from = from;
      this.to = to;
    }

    public boolean isValid(int code) {
      return code >= from && code <= to;
    }
  }
}

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

package de.gematik.utils.request;

import static de.gematik.utils.Utils.getLog;
import static de.gematik.utils.request.SSLContextFactory.getX509TrustManager;
import static okhttp3.ConnectionSpec.MODERN_TLS;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import javax.net.ssl.SSLContext;
import lombok.SneakyThrows;
import okhttp3.OkHttpClient;
import okhttp3.OkHttpClient.Builder;
import okhttp3.Request;
import okhttp3.ResponseBody;
import org.apache.maven.plugin.MojoExecutionException;

public class ApiRequester {

  private String trustStorePath;
  private String clientCertPath;
  private String trustStorePassword;
  private String clientCertPassword;
  private String proxyHost;
  private Integer proxyPort;
  private OkHttpClient client;


  public String getApiResponse(String url) throws MojoExecutionException {
    if (client == null) {
      setupAndCreateClient();
    }
    Request request = new Request.Builder().url(url).build();
    try (ResponseBody body = client.newCall(request).execute().body()) {
      return body.string();
    } catch (IOException e) {
      throw new MojoExecutionException(
          "API did not respond -> " + url + " cause: " + e.getMessage());
    }
  }

  @SneakyThrows
  @SuppressWarnings("java:S5527")
  private void setupAndCreateClient() {
    Builder builder = new Builder();
    checkAndConfigureProxy(builder);
    checkAndConfigureMtls(builder);
    client = builder.build();
  }

  private void checkAndConfigureMtls(Builder builder) throws MojoExecutionException {
    if (sslParameters().noneMatch(Objects::isNull)) {
      SSLContext sslContext = SSLContextFactory.createAndGetSSLContext(clientCertPath,
          trustStorePath, clientCertPassword,
          trustStorePassword);
      builder.hostnameVerifier((hostname, session) -> true)
          .connectionSpecs(List.of(MODERN_TLS))
          .sslSocketFactory(sslContext.getSocketFactory(), getX509TrustManager(trustStorePath, trustStorePassword))
          .build();
      getLog().info("Using mTLS");
    } else if (sslParameters().allMatch(Objects::isNull)) {
      getLog().warn("Using no TLS");
    } else {
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
      throw new MojoExecutionException(
          "You tried to set a proxy but one parameter is missing");
    }
  }

  public void setupProxy(String proxyHost, Integer proxyPort) {
    this.proxyHost = proxyHost;
    this.proxyPort = proxyPort;
  }

  public void setupTls(String trustStorePath, String trustStorePassword, String clientCertPath,
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
}

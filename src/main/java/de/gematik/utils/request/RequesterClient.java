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

package de.gematik.utils.request;

import static de.gematik.utils.Utils.getLog;
import static de.gematik.utils.request.SSLContextFactory.getX509TrustManager;
import static okhttp3.ConnectionSpec.MODERN_TLS;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.net.ssl.SSLContext;
import okhttp3.OkHttpClient;
import okhttp3.OkHttpClient.Builder;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.maven.plugin.MojoExecutionException;

public class RequesterClient {

  private String trustStorePath;
  private String clientCertPath;
  private String trustStorePassword;
  private String clientCertPassword;
  private String proxyHost;
  private List<String> noProxyHosts = new ArrayList<>();
  private Integer proxyPort;

  private OkHttpClient client;

  public RequesterClient proxy(String proxyHost, Integer proxyPort) {
    this.proxyHost = proxyHost;
    this.proxyPort = proxyPort;
    return this;
  }

  public RequesterClient noProxyHosts(String noProxyHosts) {
    this.noProxyHosts = Arrays.asList(noProxyHosts.split("[,|;]"));
    return this;
  }

  public RequesterClient tls(
      String trustStorePath,
      String trustStorePassword,
      String clientCertPath,
      String clientCertPassword) {
    this.trustStorePath = trustStorePath;
    this.trustStorePassword = trustStorePassword;
    this.clientCertPath = clientCertPath;
    this.clientCertPassword = clientCertPassword;
    return this;
  }

  public RequesterClient build() throws MojoExecutionException {
    Builder builder = new Builder();
    checkAndConfigureProxy(builder);
    checkAndConfigureMtls(builder);
    this.client = builder.build();
    return this;
  }

  private void checkAndConfigureProxy(Builder builder) throws MojoExecutionException {
    if (proxyHost != null && proxyPort != null && noProxyHosts.isEmpty()) {
      builder.proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort)));
      getLog().info("Using proxy: " + proxyHost + ":" + proxyPort);
    } else if (proxyHost == null && proxyPort == null) {
      getLog().info("Using no proxy");
    } else if (proxyHost != null && proxyPort != null) {
      builder.proxySelector(
          new ProxySelector() {
            private final Proxy proxy =
                new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));

            @Override
            public List<Proxy> select(URI uri) {
              if (noProxyHosts.stream().anyMatch(host -> uri.getHost().endsWith(host.trim()))) {
                return List.of(Proxy.NO_PROXY);
              }
              return List.of(proxy);
            }

            @Override
            public void connectFailed(URI uri, SocketAddress socketAddress, IOException ex) {
              getLog().error("Unable to reach uri " + uri + " via proxy: " + ex.getMessage());
            }
          });
      getLog()
          .info(
              "Using proxy: "
                  + proxyHost
                  + ":"
                  + proxyPort
                  + " (except for: "
                  + noProxyHosts
                  + ")");
    } else {
      throw new MojoExecutionException("You tried to set a proxy but one parameter is missing");
    }
  }

  @SuppressWarnings("java:S5527")
  private void checkAndConfigureMtls(Builder builder) throws MojoExecutionException {
    Map<String, String> variableMap = new HashMap<>();
    variableMap.put("clientCertPath", clientCertPath);
    variableMap.put("trustStorePath", trustStorePath);
    variableMap.put("clientCertPassword", clientCertPassword);
    variableMap.put("trustStorePassword", trustStorePassword);
    if (variableMap.values().stream().allMatch(Objects::nonNull)) {
      SSLContext sslContext =
          SSLContextFactory.createAndGetSSLContext(
              clientCertPath, trustStorePath, clientCertPassword, trustStorePassword);
      // just trust all hosts
      builder
          .hostnameVerifier((hostname, session) -> true)
          .connectionSpecs(List.of(MODERN_TLS))
          .sslSocketFactory(
              sslContext.getSocketFactory(),
              getX509TrustManager(trustStorePath, trustStorePassword))
          .build();
      getLog().info("Using mTLS");
    } else if (variableMap.values().stream().allMatch(Objects::isNull)) {
      getLog().info("Using no TLS");
    } else {
      for (Map.Entry<String, String> entry : variableMap.entrySet()) {
        if (entry.getValue() == null || entry.getValue().isEmpty()) {
          getLog().warn(entry.getKey() + " is missing.");
        } else {
          getLog().info(entry.getKey() + " is set.");
        }
      }
      throw new MojoExecutionException(
          "You tried to set an mTLS context but at least one parameter is missing");
    }
  }

  public Response newCall(Request request) throws IOException {
    return client.newCall(request).execute();
  }
}

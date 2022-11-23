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

package de.gematik.prepare.request;

import de.gematik.prepare.PrepareItemsMojo;
import java.io.IOException;
import javax.net.ssl.SSLContext;
import lombok.SneakyThrows;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;
import org.apache.maven.plugin.MojoExecutionException;

public class ApiRequester {

  private String trustStorePath;
  private String clientCertPath;
  private String trustStorePassword;
  private String clientCertPassword;
  private OkHttpClient client;


  public String getApiResponse(String value) throws MojoExecutionException {
    if (client == null) {
      setupAndCreateClient();
    }
    Request request = new Request.Builder().url(value).build();
    try (ResponseBody body = client.newCall(request).execute().body()) {
      return body.string();
    } catch (IOException e) {
      throw new MojoExecutionException("API did not respond -> " + value, e);
    }
  }

  @SneakyThrows
  private void setupAndCreateClient() {
    if (trustStorePath != null && trustStorePassword != null && clientCertPath != null
        && clientCertPassword != null) {
      SSLContext sslContext = SSLContextFactory.createAndGetSSLContext(clientCertPath,
          trustStorePath, clientCertPassword,
          trustStorePassword);
      client = new OkHttpClient.Builder()
          .sslSocketFactory(sslContext.getSocketFactory())
          .build();
      PrepareItemsMojo.getInstance().getLog().info("Using mTLS");
    } else if (trustStorePath == null && trustStorePassword == null && clientCertPath == null
        && clientCertPassword == null) {
      client = new OkHttpClient.Builder().build();
      PrepareItemsMojo.getInstance().getLog().warn("Using no TLS");
    } else {
      throw new MojoExecutionException(
          "You tried to set an mTLS context but one information is missing");
    }
  }

  public void setupTls(String trustStorePath, String trustStorePassword, String clientCertPath,
      String clientCertPassword) {
    this.trustStorePath = trustStorePath;
    this.trustStorePassword = trustStorePassword;
    this.clientCertPath = clientCertPath;
    this.clientCertPassword = clientCertPassword;
    setupAndCreateClient();
  }
}

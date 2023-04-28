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

import static javax.net.ssl.KeyManagerFactory.getDefaultAlgorithm;

import java.io.FileInputStream;
import java.security.KeyStore;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import lombok.SneakyThrows;

public class SSLContextFactory {

  private SSLContextFactory() {
  }

  @SneakyThrows
  public static SSLContext createAndGetSSLContext(String keyStore, String trustStore,
      String keyStorePassword, String trustStorePassword) {

    final KeyManager[] keyManagers = getKeyManagers(keyStore, keyStorePassword);
    final TrustManager[] trustManagers = getTrustManagers(trustStore, trustStorePassword);
    final SSLContext sslContext = SSLContext.getInstance("TLSv1.2");

    sslContext.init(keyManagers, trustManagers, null);

    return sslContext;
  }

  @SneakyThrows
  private static KeyManager[] getKeyManagers(String keyStore, String keyStorePassword) {
    KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(getDefaultAlgorithm());
    KeyStore ks = KeyStore.getInstance("pkcs12");
    try (FileInputStream fis = new FileInputStream(keyStore)) {
      ks.load(fis, keyStorePassword.toCharArray());
    }
    keyManagerFactory.init(ks, keyStorePassword.toCharArray());
    return keyManagerFactory.getKeyManagers();
  }

  @SneakyThrows
  private static TrustManager[] getTrustManagers(String trustStore, String trustStorePassword) {
    TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(
        getDefaultAlgorithm());
    KeyStore ks = KeyStore.getInstance("pkcs12");
    try (FileInputStream fis = new FileInputStream(trustStore)) {
      ks.load(fis, trustStorePassword.toCharArray());
    }
    trustManagerFactory.init(ks);
    return trustManagerFactory.getTrustManagers();
  }
}
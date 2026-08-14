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

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.lang3.StringUtils;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.apache.maven.plugin.MojoExecutionException;

@Named
@Singleton
public class ApiRequester {

  private final RequesterClient client;

  private List<StatusCodes> allowedFamilies;
  private List<Integer> allowedCodes;

  @Inject
  public ApiRequester(RequesterClient client) {
    this.client = client;
  }

  public String getApiResponse(String url) throws MojoExecutionException {
    Request request = new Request.Builder().url(url).build();
    try (Response response = client.newCall(request)) {
      validateCode(response.code());
      return response.body().string();
    } catch (IOException e) {
      throw new MojoExecutionException(
          "API did not respond -> " + url + " cause: " + e.getMessage());
    }
  }

  private void validateCode(int code) throws MojoExecutionException {
    if (allowedCodes == null) {
      if (allowedFamilies.stream().noneMatch(statusCode -> statusCode.isValid(code))) {
        throw new MojoExecutionException(
            "Response code was "
                + code
                + " and not in allowed response families"
                + allowedFamilies);
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

  public void setAllowedResponses(String codeFamilies, String codes) throws MojoExecutionException {
    if (StringUtils.isNotBlank(codes)) {
      try {
        List<Integer> namedCodes = Arrays.stream(codes.split(",")).map(Integer::valueOf).toList();
        if (namedCodes.stream().anyMatch(i -> i >= 600 || i < 100)) {
          throw new MojoExecutionException("Codes could only have a range of 100 - 599");
        }
        this.allowedCodes = namedCodes;
      } catch (NumberFormatException ex) {
        throw new MojoExecutionException("Invalid status code was provided in [" + codes + "]");
      }
    } else {
      try {
        allowedFamilies = Arrays.stream(codeFamilies.split(",")).map(StatusCodes::valueOf).toList();
      } catch (IllegalArgumentException e) {
        throw new MojoExecutionException(
            "Unknown status code family found in \""
                + codeFamilies
                + "\" allowed values: "
                + Arrays.toString(StatusCodes.values()));
      }
    }
  }
}

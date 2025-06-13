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

package de.gematik.combine.model.properties;

import static java.lang.Math.max;
import static java.lang.String.format;

import java.lang.reflect.MalformedParametersException;
import java.util.LinkedList;
import java.util.stream.Collectors;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Version implements Comparable<Version> {

  private final LinkedList<Integer> versionParts;

  @NonNull
  public static Version getSemanticVersion(String versionString)
      throws MalformedParametersException {
    LinkedList<Integer> versionParts = new LinkedList<>();
    try {
      for (String versionPartString : versionString.trim().split("\\.")) {
        versionParts.add(Integer.parseInt(versionPartString));
      }
    } catch (NumberFormatException ignored) {
      throw new MalformedParametersException(
          format(
              "Version may only contain numbers and dots(.), does not match: %s", versionString));
    }
    if (versionParts.isEmpty()) {
      throw new MalformedParametersException("Version may not be empty");
    }
    return new Version(versionParts);
  }

  @Override
  public int compareTo(Version o) {
    for (int i = 0; i < max(versionParts.size(), o.versionParts.size()); i++) {
      int thisPart = (i < versionParts.size()) ? versionParts.get(i) : 0;
      int otherPart = (i < o.versionParts.size()) ? o.versionParts.get(i) : 0;
      if (thisPart > otherPart) {
        return 1;
      }
      if (thisPart < otherPart) {
        return -1;
      }
    }
    return 0;
  }

  @Override
  public String toString() {
    return versionParts.stream().map(Object::toString).collect(Collectors.joining("."));
  }
}

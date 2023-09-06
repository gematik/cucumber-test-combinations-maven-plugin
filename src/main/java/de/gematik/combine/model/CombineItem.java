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

package de.gematik.combine.model;

import static de.gematik.combine.util.NonNullableMap.nonNullableMap;
import static java.util.Objects.nonNull;

import de.gematik.combine.CombineMojo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Singular;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import lombok.SneakyThrows;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class CombineItem implements Comparable<CombineItem> {

  public static final String DEFAULT_PROPERTY = "";

  private String value;
  private String url;

  @Singular
  @EqualsAndHashCode.Exclude
  private Set<String> tags = new HashSet<>();

  @Singular
  @EqualsAndHashCode.Exclude
  private Map<String, String> properties = new HashMap<>();

  @Singular
  @EqualsAndHashCode.Exclude
  private Set<String> groups = new HashSet<>();

  @EqualsAndHashCode.Exclude
  private String checkExpression;

  @EqualsAndHashCode.Exclude
  private String checkExpressionId;

  public String toString() {
    return value;
  }

  @SuppressWarnings("unused") // method is used in JEXL expressions
  public boolean hasTag(String tag) {
    return getTags().contains(tag);
  }

  @SuppressWarnings("unused") // method is used in JEXL expressions
  public boolean hasProperty(String property) {
    return properties.containsKey(property);
  }

  public Map<String, String> getProperties() {
    return nonNullableMap(properties, this::getDefaultPropertyAndLog);
  }

  @Override
  public int compareTo(CombineItem other) {
    return value.compareTo(other.value);
  }

  private String getDefaultPropertyAndLog(String key) {
    CombineMojo.getPluginLog().info(
        String.format("item %s does not have property %s", value, key));
    return DEFAULT_PROPERTY;
  }

  public String produceValueUrl() {
    return nonNull(this.url) ? value + " ---> " + url : value;
  }
}

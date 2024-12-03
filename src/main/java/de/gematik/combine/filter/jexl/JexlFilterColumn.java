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

package de.gematik.combine.filter.jexl;

import static de.gematik.combine.util.NonNullableMap.nonNullableMap;
import static java.util.stream.Collectors.toList;

import de.gematik.combine.model.TableCell;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class JexlFilterColumn {

  public static final JexlFilterList<String> DEFAULT_PROPERTY = new JexlFilterList<>();

  @Getter private final String header;

  private final List<TableCell> column;

  public Map<String, JexlFilterList<String>> getProperties() {
    List<Entry<String, String>> entryList =
        column.stream()
            .flatMap(tableCell -> tableCell.getProperties().entrySet().stream())
            .collect(toList());

    Map<String, JexlFilterList<String>> properties = new HashMap<>();

    for (Entry<String, String> entry : entryList) {
      if (!properties.containsKey(entry.getKey())) {
        properties.put(entry.getKey(), new JexlFilterList<>());
      }
      properties.get(entry.getKey()).add(entry.getValue());
    }

    return nonNullableMap(properties, key -> DEFAULT_PROPERTY);
  }

  public JexlFilterList<String> getTags() {
    List<String> tags = column.stream().flatMap(tv -> tv.getTags().stream()).collect(toList());
    return new JexlFilterList<>(tags);
  }
}

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

package de.gematik.combine.filter.table;

import static de.gematik.combine.CombineMojo.getPluginLog;
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

import de.gematik.combine.model.TableCell;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * This filter removes rows with the same property value in the given column above the given
 * maxCount.
 */
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor
public class MaxSameColumnPropertyFilter extends AbstractTableFilter {

  private final String columnName;
  private final String property;
  private final int maxCount;

  @Override
  public List<List<TableCell>> apply(List<List<TableCell>> table) {
    getPluginLog().debug(format("applying %s on %d rows", this, table.size()));
    Map<String, AtomicInteger> propCounts = new HashMap<>();
    return table.stream()
        .filter(row -> checkRow(row, propCounts))
        .collect(toList());
  }

  private boolean checkRow(List<TableCell> row, Map<String, AtomicInteger> propCounts) {
    Optional<TableCell> cell = row.stream()
        .filter(tv -> tv.getHeader().equals(columnName))
        .findAny();
    Optional<String> propValue = cell
        .map(tv -> tv.getProperties().get(property));

    if (propValue.isPresent() && !propCounts.containsKey(propValue.get())) {
      propCounts.put(propValue.get(), new AtomicInteger());
    }

    return propValue
        .map(propCounts::get)
        .map(AtomicInteger::incrementAndGet)
        .map(count -> count <= maxCount)
        .orElse(true);
  }
}

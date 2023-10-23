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

package de.gematik.combine.filter.table;

import static de.gematik.combine.CombineMojo.getPluginLog;
import static java.lang.String.format;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

import de.gematik.combine.model.TableCell;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * This filter removes rows that have the same value in the given column as an earlier row.
 */
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor
public class DistinctColumnFilter extends AbstractTableFilter {

  private final String columnName;

  @Override
  public List<List<TableCell>> apply(List<List<TableCell>> table) {
    getPluginLog().debug(format("applying %s on %d rows", this, table.size()));
    Set<String> combinationSet = new TreeSet<>();

    return table.stream()
        .filter(row -> combinationSet.add(columnValue(row)))
        .collect(toList());
  }

  private String columnValue(List<TableCell> row) {
    return row.stream()
        .map(val -> val.getHeader().equals(columnName) ? val.getValue() : "")
        .collect(joining());
  }

}

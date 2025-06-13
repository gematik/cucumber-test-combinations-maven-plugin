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
 * This filter removes rows that contain the same values as an earlier row just in different order.
 */
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor
public class DoubleLineupFilter extends AbstractTableFilter {

  private final boolean allow;

  @Override
  public List<List<TableCell>> apply(List<List<TableCell>> table) {
    if (allow) {
      return table;
    }
    getPluginLog().debug(format("applying %s on %d rows", this, table.size()));
    Set<String> combinationSet = new TreeSet<>();

    return table.stream()
        .filter(
            row -> {
              String sortedCellValues =
                  row.stream().map(TableCell::getValue).sorted().collect(joining());
              return combinationSet.add(sortedCellValues);
            })
        .collect(toList());
  }
}

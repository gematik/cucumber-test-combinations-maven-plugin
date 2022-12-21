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

package de.gematik.combine.filter;

import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;
import static java.util.stream.Collectors.toMap;

import de.gematik.combine.filter.table.TableFilter;
import de.gematik.combine.filter.table.cell.CellFilter;
import de.gematik.combine.filter.table.row.RowFilter;
import de.gematik.combine.filter.table.row.TableRowFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Filters {

  @Default
  private final List<TableFilter> tableFilters = new ArrayList<>();

  @Default
  private final List<TableRowFilter> tableRowFilters = new ArrayList<>();

  @Default
  private final Map<String, List<CellFilter>> cellFilters = new HashMap<>();

  public void addTableFilter(TableFilter tableFilter) {
    tableFilters.add(tableFilter);
  }

  public void addTableRowFilter(TableRowFilter tableRowFilter) {
    tableRowFilters.add(tableRowFilter);
  }

  public void addCellFilter(String header, CellFilter newCellFilter) {
    if (cellFilters.containsKey(header)) {
      List<CellFilter> cellFilter = cellFilters.get(header);
      cellFilter.add(newCellFilter);
    } else {
      cellFilters.put(header, new ArrayList<>(List.of(newCellFilter)));
    }
  }

  public void addCellFilter(String header, List<CellFilter> newCellFilter) {
    newCellFilter.forEach(cellFilter -> addCellFilter(header, cellFilter));
  }

  public List<TableFilter> getTableFilters() {
    return unmodifiableList(tableFilters);
  }

  public List<TableRowFilter> getTableRowFilters() {
    return unmodifiableList(tableRowFilters);
  }

  public Map<String, List<CellFilter>> getCellFilters() {
    return unmodifiableMap(cellFilters.entrySet().stream()
        .map(e -> Map.entry(e.getKey(), unmodifiableList(e.getValue())))
        .collect(toMap(Entry::getKey, Entry::getValue)));
  }

  public RowFilter combineAllRowFilters() {
    return getTableRowFilters().stream()
        .reduce(x -> true, RowFilter::and, RowFilter::and);
  }
  //TODO above combined all row filters to one evil filter, try List of filter to filter filters who have no headers jet

  public Map<String, CellFilter> combineCellFilters() {
    return unmodifiableMap(cellFilters.entrySet().stream()
        .map(e -> Map.entry(e.getKey(), e.getValue().stream()
            .reduce(x -> true, CellFilter::and)))
        .collect(toMap(Entry::getKey, Entry::getValue)));
  }

}

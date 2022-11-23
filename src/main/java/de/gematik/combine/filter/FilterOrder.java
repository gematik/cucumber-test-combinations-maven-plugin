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

import de.gematik.combine.filter.table.DistinctColumnFilter;
import de.gematik.combine.filter.table.DoubleLineupFilter;
import de.gematik.combine.filter.table.MaxRowsFilter;
import de.gematik.combine.filter.table.MaxSameColumnPropertyFilter;
import de.gematik.combine.filter.table.ShuffleTableFilter;
import de.gematik.combine.filter.table.TableFilter;
import de.gematik.combine.filter.table.row.TableRowFilter;
import java.util.Arrays;
import java.util.Set;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Defines a sorting order for {@link TableFilter} implementations.
 */
@Getter
@RequiredArgsConstructor
public enum FilterOrder {

  /**
   * Filters that operate only on rows do not interfere with other filters. Therefore, they can be applied early in filtering.
   */
  ROW_ONLY(0, Set.of(TableRowFilter.class)),

  /**
   * Shuffling should be applied before all filters that operate on the whole table.
   */
  SHUFFLE(1, Set.of(ShuffleTableFilter.class)),

  /**
   * The MaxSameColumnPropertyFilter operates on the whole table and can interfere with other filters. It can filter out rows that would not be
   * filtered if MaxSameColumnPropertyFilter is applied later. Therefore, the MaxSameColumnPropertyFilter should be applied late in filtering
   * process.
   */
  MAX_SAME_VALUE_COLUMN_PROPERTY(10, Set.of(MaxSameColumnPropertyFilter.class)),

  /**
   * The DistinctColumnFilter operates on the whole table and can interfere with other filters. It can filter out rows that would not be filtered if
   * DistinctColumnFilter is applied later. Therefore, the DistinctColumnFilter should be applied late in filtering process.
   */
  DISTINCT_COLUMN(20, Set.of(DistinctColumnFilter.class)),

  /**
   * The DoubleLineupFilter operates on the whole table and can interfere with other filters. It can filter out rows that would not be filtered if
   * DoubleLineupFilter is applied later. Therefore, the DoubleLineupFilter should be applied late in filtering process.
   */
  DOUBLE_LINEUP(30, Set.of(DoubleLineupFilter.class)),

  /**
   * The MaxRowFilter cuts the table after a defined number of rows and does not evaluate any other properties. Therefore, the MaxRowFilter should be
   * applied as last filter.
   */
  MAX_ROWS(Integer.MAX_VALUE, Set.of(MaxRowsFilter.class));

  public final int orderKey;
  private final Set<Class<? extends TableFilter>> filters;

  public static FilterOrder getFilterOrderFor(TableFilter tableFilter) {
    return Arrays.stream(values())
        .filter(fo ->
            fo.filters.contains(tableFilter.getClass()) ||
            fo.filters.contains(tableFilter.getClass().getSuperclass()))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException(
            tableFilter.getClass().getName() + " has no FilterOrder entry"));
  }
}

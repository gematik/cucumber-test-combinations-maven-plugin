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

import de.gematik.combine.FilterConfiguration;
import de.gematik.combine.filter.table.DoubleLineupFilter;
import de.gematik.combine.filter.table.MaxRowsFilter;
import de.gematik.combine.filter.table.ShuffleTableFilter;
import de.gematik.combine.filter.table.TableFilter;
import de.gematik.combine.filter.table.row.SelfCombineFilter;
import de.gematik.combine.filter.table.row.TableRowFilter;
import java.util.List;

public class ConfigFilterMapper {

  private ConfigFilterMapper() {
  }

  public static Filters toFilters(FilterConfiguration config) {
    Filters filters = new Filters();
    getRowFilters(config).forEach(filters::addTableRowFilter);
    getTableFilters(config).forEach(filters::addTableFilter);
    return filters;
  }

  private static List<TableRowFilter> getRowFilters(FilterConfiguration config) {
    return List.of(
        new SelfCombineFilter(config.isAllowSelfCombine())
    );
  }

  private static List<TableFilter> getTableFilters(FilterConfiguration config) {
    return List.of(
        new ShuffleTableFilter(config.isShuffleCombinations()),
        new DoubleLineupFilter(config.isAllowDoubleLineup()),
        new MaxRowsFilter("" + config.getMaxTableRows())
    );
  }

}

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

package de.gematik.combine.filter.table;

import static de.gematik.combine.filter.FilterOrder.getFilterOrderFor;
import static java.lang.Integer.compare;

import de.gematik.combine.filter.FilterOrder;
import de.gematik.combine.model.TableCell;
import java.util.List;
import java.util.function.UnaryOperator;

/**
 * A TableFilter operates on the whole table and can therefore implement filters that need more than
 * a single row. If your filter operates only on rows, you should look at
 * {@link  de.gematik.combine.filter.table.row.TableRowFilter TableRowFilter}.
 */
public interface TableFilter extends UnaryOperator<List<List<TableCell>>>,
    Comparable<TableFilter> {

  default FilterOrder getFilterOrder() {
    return getFilterOrderFor(this);
  }

  @Override
  default int compareTo(TableFilter filter) {
    return compare(getFilterOrder().orderKey, filter.getFilterOrder().orderKey);
  }

  default TableFilter combine(TableFilter after) {
    return v -> UnaryOperator.super.andThen(after).apply(v);
  }
}

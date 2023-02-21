/*
 * Copyright (c) 2023 gematik GmbH
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

package de.gematik.combine.filter.table.cell;

import de.gematik.combine.filter.project.ProjectCellFilter;
import de.gematik.combine.model.TableCell;
import java.util.function.Predicate;

/**
 * A {@link CellFilter} is a Filter that operates on a single table cell and can filter each value
 * with no dependencies to other values. Therefore, it can be applied to the column values before
 * generating the cartesian product and reduce the size of the produced base table significantly. A
 * CellFilter can override a {@link ProjectCellFilter}
 */
public interface CellFilter extends Predicate<TableCell> {

  default CellFilter and(CellFilter other) {
    return value -> Predicate.super.and(other).test(value);
  }
}

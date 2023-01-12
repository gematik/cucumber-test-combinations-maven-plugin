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

package de.gematik.combine.filter.table.row;

import de.gematik.combine.model.TableCell;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

/**
 * A RowFilter is a Filter that operates on a whole row of values and therefore cannot be applied before the creation of the table.
 * Only after the rows have been combined using the cartesian product may rows be filtered with a RowFilter.
 */
public interface RowFilter extends Predicate<List<TableCell>> {

  default List<String> getRequiredColumns(List<String> headers){
    return Collections.emptyList();
  }
  default RowFilter and(RowFilter other) {
    return row -> Predicate.super.and(other).test(row);
  }
}

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

package de.gematik.combine.filter.table.row;

import de.gematik.combine.model.TableCell;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

/** This filter removes rows where not all entries have different values for a given property. */
@EqualsAndHashCode(callSuper = false)
@RequiredArgsConstructor
public class DistinctRowPropertyFilter extends TableRowFilter {

  private final String property;

  @Override
  public boolean test(List<TableCell> tableCells) {
    long distinctProperties =
        tableCells.stream()
            .map(cell -> cell.getCombineItem().getProperties().get(property))
            .distinct()
            .count();
    return distinctProperties == tableCells.size();
  }
}

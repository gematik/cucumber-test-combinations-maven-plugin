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

package de.gematik.combine.filter.table.row;

import de.gematik.combine.model.TableCell;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/** This filter removes rows where not all entries have the same value for a given property. */
@ToString
@EqualsAndHashCode(callSuper = false)
@RequiredArgsConstructor
public class EqualRowPropertyFilter extends TableRowFilter {

  private final String property;

  @Override
  public boolean test(List<TableCell> tableRow) {
    long distinctValues =
        tableRow.stream()
            .map(val -> val.getCombineItem().getProperties().get(property))
            .distinct()
            .count();
    return distinctValues == 1;
  }
}

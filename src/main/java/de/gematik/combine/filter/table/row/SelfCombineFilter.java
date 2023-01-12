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

import static java.util.stream.Collectors.toSet;

import de.gematik.combine.model.TableCell;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * This filter removes rows in which a distinct value appears more than once.
 */
@ToString
@RequiredArgsConstructor
public class SelfCombineFilter extends TableRowFilter {

  private final boolean allow;

  @Override
  public boolean test(List<TableCell> tableRow) {
    if (allow) {
      return true;
    }
    Set<String> rowValues = tableRow.stream()
        .map(TableCell::getValue)
        .collect(toSet());
    return rowValues.size() == tableRow.size();
  }

}

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
import lombok.ToString;

/** This filter removes rows where none of the entries have a certain given tag. */
@ToString
@EqualsAndHashCode(callSuper = false)
@RequiredArgsConstructor
public class RequireTagRowFilter extends TableRowFilter {

  private final String tag;

  @Override
  public boolean test(List<TableCell> tableRow) {
    return tableRow.stream().anyMatch(cell -> cell.hasTag(tag));
  }
}

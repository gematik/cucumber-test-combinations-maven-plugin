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

package de.gematik.combine.tags;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import de.gematik.combine.filter.table.AbstractTableFilter;
import de.gematik.combine.filter.table.TableFilter;
import de.gematik.combine.filter.table.cell.AbstractCellFilter;
import de.gematik.combine.filter.table.cell.CellFilter;
import de.gematik.combine.filter.table.row.TableRowFilter;
import de.gematik.combine.model.TableCell;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ParsedTagsTest {

  @ParameterizedTest
  @CsvSource({
    "true,false,false",
    "true,true,false",
    "true,true,true",
    "false,true,false",
    "true,false,true",
    "false,false,false",
    "false,false,true"
  })
  void returnFalseIfNoSoftFilterIncluded(
      boolean softTableFilter, boolean softTableRowFilter, boolean softCellFilter) {
    // arrange
    ParsedTags parsedTags = new ParsedTags(new ArrayList<>());

    TableFilter t = getTableFilter(softTableFilter);
    parsedTags.addTableFilter(t);
    TableRowFilter tr = getTableRowFilter(softTableRowFilter);
    parsedTags.addTableRowFilter(tr);
    CellFilter c = getCellFilter(softCellFilter);
    parsedTags.addCellFilter("Header_1", c);
    // assert
    assertThat(parsedTags.containSoftFilter())
        .isEqualTo(softCellFilter || softTableFilter || softTableRowFilter);
  }

  private CellFilter getCellFilter(boolean softCellFilter) {
    return (CellFilter)
        new AbstractCellFilter() {
          @Override
          public boolean test(TableCell tableCell) {
            return false;
          }
        }.setSoft(softCellFilter);
  }

  private TableRowFilter getTableRowFilter(boolean softTableRowFilter) {
    return (TableRowFilter)
        new TableRowFilter() {
          @Override
          public boolean test(List<TableCell> tableCells) {
            return false;
          }
        }.setSoft(softTableRowFilter);
  }

  private TableFilter getTableFilter(boolean softTableFilter) {
    return new AbstractTableFilter() {
      @Override
      public List<List<TableCell>> apply(List<List<TableCell>> t) {
        return null;
      }
    }.setSoft(softTableFilter);
  }
}

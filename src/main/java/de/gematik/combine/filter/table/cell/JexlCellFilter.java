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

package de.gematik.combine.filter.table.cell;

import de.gematik.combine.filter.jexl.JexlFilter;
import de.gematik.combine.model.TableCell;
import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;

/**
 * This filter executes a <a
 * href="https://commons.apache.org/proper/commons-jexl/reference/syntax.html">JEXL-Expression</a>-{@link
 * java.util.function.Predicate Predicate} for a single cell value and filters accordingly.
 */
@EqualsAndHashCode
public class JexlCellFilter extends AbstractCellFilter {

  private final String columnName;
  private final JexlFilter jexlFilter;

  public JexlCellFilter(String columnName, String filterExpression) {
    this.columnName = columnName;
    this.jexlFilter = new JexlFilter(filterExpression);
  }

  @SneakyThrows
  @Override
  public boolean test(TableCell tableCell) {
    if (!columnName.equals(tableCell.getHeader())) {
      return true;
    }
    jexlFilter.addToContext(tableCell);
    return jexlFilter.evaluate();
  }

  @Override
  public String toString() {
    return "JexlCellFilter(\"" + jexlFilter + "\")";
  }
}

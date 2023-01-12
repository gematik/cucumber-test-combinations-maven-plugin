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

package de.gematik.combine.filter.table;

import static de.gematik.combine.CombineMojo.getPluginLog;
import static de.gematik.combine.filter.FilterOrder.MAX_ROWS;
import static java.lang.Math.min;
import static java.lang.String.format;

import de.gematik.combine.filter.FilterOrder;
import de.gematik.combine.filter.jexl.JexlFilter;
import de.gematik.combine.model.TableCell;
import java.util.List;
import lombok.EqualsAndHashCode;

/**
 * This filter removes rows above the given count. The count can also be a <a
 * href="https://commons.apache.org/proper/commons-jexl/reference/syntax.html">JEXL-Expression</a>
 * that returns an Integer.
 */
@EqualsAndHashCode
public class MaxRowsFilter implements TableFilter {

  private final JexlFilter jexlFilter;

  public MaxRowsFilter(String filterExpression) {
    this.jexlFilter = new JexlFilter(filterExpression);
  }

  @Override
  public List<List<TableCell>> apply(List<List<TableCell>> table) {
    jexlFilter.addTableInfoToContext(table);
    int maxRows = jexlFilter.evaluate();
    getPluginLog().debug(format("applying %s on %d rows", this, table.size()));
    return table.subList(0, min(table.size(), maxRows));
  }

  @Override
  public FilterOrder getFilterOrder() {
    return MAX_ROWS;
  }

  @Override
  public String toString() {
    return "MaxRowsFilter(\"" + jexlFilter + "\")";
  }
}

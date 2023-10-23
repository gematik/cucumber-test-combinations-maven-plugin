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

import static java.util.stream.Collectors.toList;

import de.gematik.combine.filter.jexl.JexlFilter;
import de.gematik.combine.model.TableCell;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;
import lombok.ToString;

/**
 * This filter executes a <a
 * href="https://commons.apache.org/proper/commons-jexl/reference/syntax.html">JEXL-Expression</a>-{@link java.util.function.Predicate Predicate}
 * for each row and filters accordingly.
 */
@ToString
@EqualsAndHashCode(callSuper = false)
public class JexlRowFilter extends TableRowFilter {

  final JexlFilter jexlFilter;

  public JexlRowFilter(String filterExpression) {
    this.jexlFilter = new JexlFilter(filterExpression);
  }

  @Override
  @SneakyThrows
  public boolean test(List<TableCell> tableRow) {
    jexlFilter.addToContext(tableRow);
    return this.jexlFilter.evaluate();
  }

  @Override
  public List<String> getRequiredColumns(List<String> headers) {
    return headers.stream()
        .filter(header -> jexlFilter.toString().contains(header))
        .collect(toList());
  }
}

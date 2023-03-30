/*
 * Copyright 20023 gematik GmbH
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

package de.gematik.combine.tags.parser;

import static de.gematik.combine.tags.parser.FilterTagParser.JEXL_ROW_FILTER_TAG;
import static java.util.stream.Collectors.toList;

import de.gematik.combine.filter.table.cell.JexlCellFilter;
import de.gematik.combine.filter.table.row.JexlRowFilter;
import de.gematik.combine.tags.ParsedTags;
import de.gematik.combine.tags.SingleTagParser;
import java.util.List;
import javax.inject.Named;
import javax.inject.Singleton;

@Named(JEXL_ROW_FILTER_TAG)
@Singleton
@SuppressWarnings("unused")
public class FilterTagParser implements SingleTagParser {

  public static final String JEXL_ROW_FILTER_TAG = "Filter";

  @Override
  public void parseTagAndRegister(String expression, ParsedTags parsedTags) {
    List<String> columnReferences = countColumnReferences(expression, parsedTags.getColumns());
    if (columnReferences.size() == 1) {
      String column = columnReferences.get(0);
      parsedTags.addCellFilter(column,
          new JexlCellFilter(column, expression));
    } else {
      parsedTags.addTableRowFilter(new JexlRowFilter(expression));
    }
  }

  private List<String> countColumnReferences(String expression, List<String> columns) {
    return columns.stream()
        .filter(expression::contains)
        .collect(toList());
  }
}

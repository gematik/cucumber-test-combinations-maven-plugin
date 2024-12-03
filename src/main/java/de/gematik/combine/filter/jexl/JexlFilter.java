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

package de.gematik.combine.filter.jexl;

import static de.gematik.combine.filter.jexl.JexlContextVariables.ALL_PROPERTIES;
import static de.gematik.combine.filter.jexl.JexlContextVariables.ALL_TAGS;
import static de.gematik.combine.filter.jexl.JexlContextVariables.COLUMN_COUNT;
import static de.gematik.combine.filter.jexl.JexlContextVariables.ROW_COUNT;
import static java.lang.String.format;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

import de.gematik.combine.model.TableCell;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;
import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlContext;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.JexlExpression;
import org.apache.commons.jexl3.MapContext;
import org.apache.maven.plugin.MojoExecutionException;

@EqualsAndHashCode
public class JexlFilter {

  public static final JexlEngine JEXL_ENGINE =
      new JexlBuilder().strict(true).silent(false).safe(false).create();

  private final JexlExpression expression;

  @EqualsAndHashCode.Exclude private final JexlContext context = new MapContext();

  public JexlFilter(String filterExpression) {
    this.expression = JEXL_ENGINE.createExpression(filterExpression);
  }

  public <T> T evaluate() {
    return evaluate(context);
  }

  public <T> void addToContext(String key, T value) {
    context.set(key, value);
  }

  public void addToContext(JexlFilterColumn jexlFilterColumn) {
    addToContext(jexlFilterColumn.getHeader(), jexlFilterColumn);
  }

  public void addToContext(TableCell tableCell) {
    addToContext(tableCell.getHeader(), tableCell.getCombineItem());
  }

  public void addToContext(List<TableCell> tableRow) {
    tableRow.forEach(this::addToContext);
    tableRow.forEach(
        cell -> {
          addTags(cell.getTags());
          addProperties(cell.getProperties());
        });
    addToContext(COLUMN_COUNT.key, tableRow.size());
  }

  public void addTableInfoToContext(List<List<TableCell>> table) {
    addToContext(ROW_COUNT.key, table.size());
    if (table.isEmpty()) {
      return;
    }
    addToContext(COLUMN_COUNT.key, table.get(0).size());
    List<JexlFilterColumn> columns = toColumns(table);
    for (var col : columns) {
      addToContext(col);
    }
  }

  @SuppressWarnings("unchecked")
  private void addTags(Set<String> tags) {
    Map<String, Integer> allTags = (Map<String, Integer>) context.get(ALL_TAGS.key);
    if (allTags == null) {
      allTags = new HashMap<>();
      context.set(ALL_TAGS.key, allTags);
    }
    final Map<String, Integer> finalAllTags = allTags;
    tags.forEach(tag -> finalAllTags.put(tag, finalAllTags.getOrDefault(tag, 0) + 1));
  }

  @SuppressWarnings("unchecked")
  private void addProperties(Map<String, String> properties) {
    Map<String, Set<String>> allProps = (Map<String, Set<String>>) context.get(ALL_PROPERTIES.key);
    if (allProps == null) {
      allProps = new HashMap<>();
      context.set(ALL_PROPERTIES.key, allProps);
    }
    final Map<String, Set<String>> finalAllProps = allProps;
    properties.forEach(
        (key, value) -> {
          if (!finalAllProps.containsKey(key)) {
            finalAllProps.put(key, new HashSet<>());
          }
          finalAllProps.get(key).add(value);
        });
  }

  private List<JexlFilterColumn> toColumns(List<List<TableCell>> table) {
    Map<String, List<TableCell>> columns =
        table.stream().flatMap(Collection::stream).collect(groupingBy(TableCell::getHeader));

    return columns.entrySet().stream()
        .map(kv -> new JexlFilterColumn(kv.getKey(), kv.getValue()))
        .collect(toList());
  }

  @SneakyThrows
  @SuppressWarnings("unchecked")
  private <T> T evaluate(JexlContext context) {
    try {
      return (T) this.expression.evaluate(context);
    } catch (Exception e) {
      throw new MojoExecutionException(
          format(
              "Could not evaluate expression '%s': %s", expression.getSourceText(), e.getMessage()),
          e);
    }
  }

  @Override
  public String toString() {
    return expression.getSourceText();
  }
}

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

package de.gematik.combine.execution;

import static com.google.common.collect.Lists.cartesianProduct;
import static de.gematik.combine.CombineMojo.ErrorType.MINIMAL_TABLE;
import static de.gematik.combine.CombineMojo.getPluginLog;
import static de.gematik.combine.util.CurrentScenario.getCurrenScenarioName;
import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.Collections.shuffle;
import static java.util.Comparator.comparingInt;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.Stream.concat;

import de.gematik.combine.CombineMojo;
import de.gematik.combine.filter.table.cell.CellFilter;
import de.gematik.combine.filter.table.row.RowFilter;
import de.gematik.combine.model.CombineItem;
import de.gematik.combine.model.TableCell;
import de.gematik.combine.tags.ConfiguredFilters;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import lombok.Data;

/**
 * This TableGenerator combines given {@link CombineItem}s to tables. It knows two generation
 * modes:
 * <p> 1. full table: applies cell filters to columns and calculates the cartesian product
 * afterwards
 * <p> 2. minimal table: tries to use every item just once, but reuses items to fill otherwise
 * incomplete rows. Table generation complies with cell and row filters
 */
public class TableGenerator {

  private static final int ONE_MILLION = 1000000;

  public List<List<TableCell>> generateTable(List<CombineItem> combineItems,
      ConfiguredFilters filters) {
    if (filters.getActualConfig().isMinimalTable()) {
      return generateMinimalTable(combineItems, filters);
    }
    return generateFullTable(combineItems, filters);
  }

  private List<List<TableCell>> generateFullTable(List<CombineItem> combineItems,
      ConfiguredFilters filters) {
    List<String> columns = filters.getColumns();

    List<List<TableCell>> preparedColumns = preFilteredColumns(combineItems, filters);

    getPluginLog().debug(
        format("creating cartesianProduct table with %d columns", columns.size()));
    if (columns.size() > 5) {
      getPluginLog().warn(
          format("creating cartesianProduct table with %d columns produces a huge amount "
              + "of entries and can cause out of memory errors", columns.size()));
    }
    List<List<TableCell>> table = new ArrayList<>(cartesianProduct(preparedColumns));

    getPluginLog().debug(
        format("created table with %d columns and %d rows", columns.size(), table.size()));
    if (table.size() > ONE_MILLION) {
      getPluginLog().warn(
          "Such a big table will need chunking and a considerable amount of time "
              + "for filtering.\nPlease use '@Filter' tags with just one header reference to "
              + "filter columns before applying the cartesian product and therefore reduce "
              + "generated table size.");
    }
    return table;
  }

  /**
   * applies cell filters to each column and returns a list with the possible values for each
   * column
   */
  private List<List<TableCell>> preFilteredColumns(List<CombineItem> combineItems,
      ConfiguredFilters filters) {
    List<String> headers = filters.getColumns();
    getPluginLog().debug("Applied cell filters: " + filters.getCellFilters());
    Map<String, CellFilter> cellFilters = filters.combineCellFilters();

    List<List<TableCell>> preparedColumns = new ArrayList<>();
    for (String header : headers) {
      List<TableCell> e = combineItems.stream()
          .map(s -> new TableCell(header, s))
          .filter(cellFilters.getOrDefault(header, x -> true))
          .collect(toList());

      if (filters.getActualConfig().isShuffleCombinations()) {
        shuffle(e);
      }
      preparedColumns.add(e);
    }
    getPluginLog().debug(
        format("prepared columns after applied cell filters: %s", preparedColumns));

    return preparedColumns;
  }

  private List<List<TableCell>> generateMinimalTable(List<CombineItem> combineItems,
      ConfiguredFilters filters) {
    List<String> columns = filters.getColumns();
    getPluginLog().debug(format("creating minimal table with %d columns", columns.size()));

    final List<List<TableCell>> preparedColumns = preFilteredColumns(combineItems, filters);

    boolean anyColumnEmpty = preparedColumns.stream().anyMatch(List::isEmpty);
    if (anyColumnEmpty) {
      getPluginLog().debug("could not generate any row for minimal table");
      return emptyList();
    }
    getPluginLog().debug("Applied row filter: " + filters.getTableRowFilters());
    getPluginLog().debug("Applied configuration: " + filters.getActualConfig());

    StateInfo stateInfo = new StateInfo(filters, preparedColumns);
    forEachUnusedValue(stateInfo,
        unusedValue -> findValidRow(stateInfo, unusedValue).ifPresent(stateInfo::addNewRow));
    if (stateInfo.getAllMissingValues().size() != 0) {
      stateInfo.getAllMissingValues()
          .forEach(e -> CombineMojo.appendError(
              format("Building minimal table failed for scenario: \"%s\". "
                      + "No row could be build for -> value: %s%s",
                  getCurrenScenarioName(),
                  e.getValue(), nonNull(e.getUrl()) ? " url: " + e.getUrl() : ""),
              MINIMAL_TABLE));
    }
    return stateInfo.getResult();
  }

  private void forEachUnusedValue(StateInfo stateInfo, Consumer<TableCell> unusedValueConsumer) {
    for (List<TableCell> preparedColumn : stateInfo.getPreparedColumns()) {
      for (TableCell tableCell : preparedColumn) {
        if (stateInfo.getAllMissingValues().contains(tableCell.getCombineItem())) {
          Optional.of(tableCell).ifPresent(unusedValueConsumer);
        }
      }
    }
  }

  private Optional<List<TableCell>> findValidRow(StateInfo stateInfo, TableCell firstValue) {
    List<String> columns = stateInfo.getFilters().getColumns();
    List<List<TableCell>> preparedColumns = stateInfo.getPreparedColumns();

    Optional<List<TableCell>> completeRow = forEachColumnExtendRow(firstValue, columns,
        preparedColumns, (row, possibleValues) -> addNewValueToRow(stateInfo, row, possibleValues));

    return completeRow.map(row -> sortRowForColumns(columns, row));
  }

  private Optional<List<TableCell>> addNewValueToRow(StateInfo stateInfo, List<TableCell> row,
      List<TableCell> possibleValues) {
    List<TableCell> missingColumnValues = possibleValues.stream()
        .filter(val -> stateInfo.getAllMissingValues().contains(val.getCombineItem()))
        .collect(toList());
    List<RowFilter> rowFilters = new ArrayList<>(stateInfo.getFilters().getTableRowFilters());
    Optional<TableCell> newColValue = findNewRowValue(stateInfo, possibleValues,
        missingColumnValues, row, rowFilters);

    return newColValue.map(value -> {
      List<TableCell> extendedRow = new ArrayList<>(row);
      extendedRow.add(value);
      return extendedRow;
    });
  }

  private Optional<List<TableCell>> forEachColumnExtendRow(TableCell firstValue,
      List<String> columns, List<List<TableCell>> preparedColumns,
      BiFunction<List<TableCell>, List<TableCell>, Optional<List<TableCell>>> rowExtender) {
    int startColumn = columns.indexOf(firstValue.getHeader());
    List<TableCell> tmpRow = new ArrayList<>(columns.size());
    tmpRow.add(firstValue);
    Optional<List<TableCell>> row = Optional.of(tmpRow);

    for (int currentColumn = (startColumn + 1) % columns.size(); currentColumn != startColumn;
        currentColumn = (currentColumn + 1) % columns.size()) {
      List<TableCell> possibleValues = preparedColumns.get(currentColumn);
      row = rowExtender.apply(row.get(), possibleValues);

      if (row.isEmpty()) {
        getPluginLog().debug("could not create a valid row for: " + firstValue);
        return row;
      }
    }
    return row;
  }

  private Optional<TableCell> findNewRowValue(StateInfo stateInfo,
      List<TableCell> preparedValues, List<TableCell> remainingValuesForThisColumn,
      List<TableCell> row, List<RowFilter> rowFilter) {

    return findNewValueNotContainedIn(stateInfo, remainingValuesForThisColumn, row,
        stateInfo.getAllUsedValues(), rowFilter)
        .or(() -> findNewValueNotContainedIn(stateInfo, remainingValuesForThisColumn, row,
            emptySet(),
            rowFilter))
        .or(() -> findNewValueMatchingFilter(stateInfo, preparedValues, row, rowFilter));
  }

  private Optional<TableCell> findNewValueMatchingFilter(StateInfo stateInfo,
      List<TableCell> possibleValues,
      List<TableCell> row, List<RowFilter> rowFilters) {
    for (TableCell possibleValue : possibleValues) {
      ArrayList<TableCell> extendedRow = new ArrayList<>(row);
      extendedRow.add(possibleValue);
      if (checkRowFilter(extendedRow, rowFilters, stateInfo.getFilters().getColumns())) {
        return Optional.of(possibleValue);
      }
    }
    return Optional.empty();
  }

  private Optional<TableCell> findNewValueNotContainedIn(StateInfo stateInfo,
      List<TableCell> possibleValues,
      List<TableCell> usedRowValues, Set<CombineItem> allUsedValues, List<RowFilter> rowFilters) {
    List<String> allColumns = stateInfo.getFilters().getColumns();
    Set<CombineItem> usedCombineItems = concat(
        allUsedValues.stream(),
        usedRowValues.stream().map(TableCell::getCombineItem))
        .collect(toSet());

    for (TableCell possibleValue : possibleValues) {
      ArrayList<TableCell> possibleRow = new ArrayList<>(usedRowValues);
      possibleRow.add(possibleValue);
      if (!usedCombineItems.contains(possibleValue.getCombineItem()) && checkRowFilter(possibleRow,
          rowFilters, allColumns)) {
        return Optional.of(possibleValue);
      }
    }
    return Optional.empty();
  }

  private boolean checkRowFilter(List<TableCell> row, List<RowFilter> rowFilters,
      List<String> allHeaders) {
    return rowFilters.stream()
        .filter(rf -> isApplicable(row, rf, allHeaders))
        .allMatch(rf -> rf.test(row));
  }

  private boolean isApplicable(List<TableCell> row, RowFilter rowFilter, List<String> allHeaders) {
    List<String> requiredColumns = rowFilter.getRequiredColumns(allHeaders);
    Set<String> filledColumns = row.stream()
        .map(TableCell::getHeader)
        .collect(toSet());
    return filledColumns.containsAll(requiredColumns);
  }

  private List<TableCell> sortRowForColumns(List<String> columns, List<TableCell> row) {
    row.sort(comparingInt(a -> columns.indexOf(a.getHeader())));
    return row;
  }

  @Data
  public static class StateInfo {

    private final ConfiguredFilters filters;
    private final List<List<TableCell>> preparedColumns;
    private final Set<CombineItem> allMissingValues;
    private final Set<CombineItem> allUsedValues = new TreeSet<>();
    private final List<List<TableCell>> result = new ArrayList<>();

    public StateInfo(ConfiguredFilters filters, List<List<TableCell>> preparedColumns) {
      this.filters = filters;
      this.preparedColumns = preparedColumns;
      this.allMissingValues = compute(preparedColumns);
    }

    private Set<CombineItem> compute(List<List<TableCell>> preparedColumns) {
      return preparedColumns.stream()
          .flatMap(List::stream)
          .map(TableCell::getCombineItem)
          .collect(toSet());
    }

    public void addNewRow(List<TableCell> row) {
      result.add(row);
      Set<CombineItem> rowValues = row.stream()
          .map(TableCell::getCombineItem)
          .collect(toSet());
      allUsedValues.addAll(rowValues);
      allMissingValues.removeAll(rowValues);
    }
  }
}

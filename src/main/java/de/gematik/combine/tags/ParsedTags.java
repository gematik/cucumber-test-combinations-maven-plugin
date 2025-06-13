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

import static de.gematik.combine.filter.ConfigFilterMapper.toFilters;
import static de.gematik.combine.util.NonNullableMap.nonNullableMap;
import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static java.util.Objects.nonNull;

import de.gematik.combine.CombineConfiguration;
import de.gematik.combine.FilterConfiguration;
import de.gematik.combine.ProjectFilters;
import de.gematik.combine.filter.Filters;
import de.gematik.combine.filter.table.TableFilter;
import de.gematik.combine.filter.table.cell.CellFilter;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;

@RequiredArgsConstructor
public class ParsedTags {

  private final List<String> columns;

  private final List<ConfigModifier> configModifiers = new ArrayList<>();

  @Delegate private final Filters filters = new Filters();

  public void addConfigModifier(ConfigModifier configurationTag) {
    configModifiers.add(configurationTag);
  }

  public FilterConfiguration getActualConfig(FilterConfiguration defaultConfig) {
    return configModifiers.stream().reduce(x -> x, ConfigModifier::merge).apply(defaultConfig);
  }

  public ConfiguredFilters configureFilters(
      CombineConfiguration config, boolean softFilterShouldApply) {
    FilterConfiguration actualConfig = getActualConfig(config.getFilterConfiguration());

    Filters allFilters = toFilters(actualConfig);

    getTableFilters().stream()
        .filter(f -> !(f.isSoft() && !softFilterShouldApply))
        .forEach(allFilters::addTableFilter);
    getTableRowFilters().stream()
        .filter(f -> !(f.isSoft() && !softFilterShouldApply))
        .forEach(allFilters::addTableRowFilter);
    getCellFilters()
        .forEach((key, value) -> allFilters.addCellFilter(key, value, softFilterShouldApply));
    checkAndAddProjectFilters(config, allFilters);
    return new ConfiguredFilters(actualConfig, columns, allFilters);
  }

  public List<String> getColumns() {
    return unmodifiableList(columns);
  }

  public List<ConfigModifier> getConfigModifiers() {
    return unmodifiableList(configModifiers);
  }

  private void checkAndAddProjectFilters(CombineConfiguration config, Filters allFilters) {
    ProjectFilters projectFilters = config.getProjectFilters();
    if (nonNull(projectFilters)) {
      addRowFilters(allFilters, projectFilters);
      columns.forEach(header -> addCellFilters(header, allFilters, projectFilters));
    }
  }

  private static void addRowFilters(Filters allFilters, ProjectFilters projectFilters) {
    projectFilters.getRowFilters().stream()
        .filter(rowFilter -> isFilterOverriddenInScenario(rowFilter, allFilters, null))
        .forEach(allFilters::addTableRowFilter);
  }

  private static void addCellFilters(
      String header, Filters allFilters, ProjectFilters projectFilters) {
    projectFilters.getCellFilters().stream()
        .filter(cellFilter -> isFilterOverriddenInScenario(cellFilter, allFilters, header))
        .forEach(cellFilter -> allFilters.addCellFilter(header, cellFilter));
  }

  private static boolean isFilterOverriddenInScenario(
      Object filter, Filters allFilters, String header) {
    if (nonNull(header)) {
      return nonNullableMap(allFilters.getCellFilters(), x -> emptyList()).get(header).stream()
          .noneMatch(filter.getClass().getSuperclass()::isInstance);
    } else {
      return allFilters.getTableRowFilters().stream()
          .noneMatch(filter.getClass().getSuperclass()::isInstance);
    }
  }

  public boolean containSoftFilter() {
    return tableFiltersHaveSoft() || cellFiltersHaveSoft() || tableRowFiltersHaveSoft();
  }

  private boolean tableRowFiltersHaveSoft() {
    return getTableRowFilters().stream().anyMatch(TableFilter::isSoft);
  }

  private boolean tableFiltersHaveSoft() {
    return getTableFilters().stream().anyMatch(TableFilter::isSoft);
  }

  private boolean cellFiltersHaveSoft() {
    return getCellFilters().entrySet().stream()
        .anyMatch(s -> s.getValue().stream().anyMatch(CellFilter::isSoft));
  }
}

/*
 * Copyright (c) 2022 gematik GmbH
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

package de.gematik.combine.tags;


import static de.gematik.combine.filter.ConfigFilterMapper.toFilters;
import static java.util.Collections.unmodifiableList;

import de.gematik.combine.FilterConfiguration;
import de.gematik.combine.filter.Filters;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;

@RequiredArgsConstructor
public class ParsedTags {

  private final List<String> columns;

  private final List<ConfigModifier> configModifiers = new ArrayList<>();

  @Delegate
  private final Filters filters = new Filters();

  public void addConfigModifier(ConfigModifier configurationTag) {
    configModifiers.add(configurationTag);
  }

  public FilterConfiguration getActualConfig(FilterConfiguration defaultConfig) {
    return configModifiers.stream()
        .reduce(x -> x, ConfigModifier::merge)
        .apply(defaultConfig);
  }

  public ConfiguredFilters configureFilters(FilterConfiguration defaultConfig) {
    FilterConfiguration actualConfig = getActualConfig(defaultConfig);

    Filters allFilters = toFilters(actualConfig);

    getTableFilters().forEach(allFilters::addTableFilter);
    getTableRowFilters().forEach(allFilters::addTableRowFilter);
    getCellFilters().forEach(allFilters::addCellFilter);

    return new ConfiguredFilters(actualConfig, columns, allFilters);
  }

  public List<String> getColumns() {
    return unmodifiableList(columns);
  }

  public List<ConfigModifier> getConfigModifiers() {
    return unmodifiableList(configModifiers);
  }
}

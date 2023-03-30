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

package de.gematik.combine.tags;


import static java.util.Collections.unmodifiableList;

import de.gematik.combine.FilterConfiguration;
import de.gematik.combine.filter.Filters;
import de.gematik.combine.filter.table.TableFilter;
import java.util.List;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;

@RequiredArgsConstructor
public class ConfiguredFilters {

  @Getter
  private final FilterConfiguration actualConfig;
  private final List<String> columns;

  @Delegate
  private final Filters filters;


  public TableFilter combineAllFilters() {
    return Stream.concat(getTableFilters().stream(), getTableRowFilters().stream())
        .sorted()
        .reduce(x -> x, TableFilter::combine);
  }

  public List<String> getColumns() {
    return unmodifiableList(columns);
  }

}

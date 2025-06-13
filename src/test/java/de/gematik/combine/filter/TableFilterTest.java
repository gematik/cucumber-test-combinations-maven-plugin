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

package de.gematik.combine.filter;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

import de.gematik.combine.filter.table.DistinctColumnFilter;
import de.gematik.combine.filter.table.DoubleLineupFilter;
import de.gematik.combine.filter.table.MaxRowsFilter;
import de.gematik.combine.filter.table.TableFilter;
import de.gematik.combine.filter.table.row.DistinctRowPropertyFilter;
import de.gematik.combine.filter.table.row.JexlRowFilter;
import de.gematik.combine.filter.table.row.SelfCombineFilter;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class TableFilterTest {

  @Test
  void shouldOrderTableFilter() {
    List<TableFilter> filters =
        Stream.of(
                new MaxRowsFilter("1"),
                new DistinctRowPropertyFilter("lala"),
                new DoubleLineupFilter(false),
                new JexlRowFilter("expression"),
                new DistinctColumnFilter("column"),
                new SelfCombineFilter(false))
            .sorted()
            .collect(toList());

    assertThat(filters)
        .extracting(f -> f.getClass().getSimpleName())
        .containsExactly(
            "DistinctRowPropertyFilter",
            "JexlRowFilter",
            "SelfCombineFilter",
            "DistinctColumnFilter",
            "DoubleLineupFilter",
            "MaxRowsFilter");
  }
}

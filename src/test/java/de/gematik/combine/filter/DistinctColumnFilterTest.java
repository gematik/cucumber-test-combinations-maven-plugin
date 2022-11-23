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

package de.gematik.combine.filter;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.gematik.combine.CombineMojo;
import de.gematik.combine.filter.table.DistinctColumnFilter;
import de.gematik.combine.model.CombineItem;
import de.gematik.combine.model.TableCell;
import java.util.List;
import lombok.SneakyThrows;
import org.apache.maven.plugin.logging.Log;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DistinctColumnFilterTest {

  public static final String COLUMN_NAME = "A";

  Log log;

  @BeforeEach
  public void setup() {
    CombineMojo mojo = mock(CombineMojo.class);
    log = mock(Log.class);
    when(mojo.getLog()).thenReturn(log);
    CombineMojo.setInstance(mojo);
  }

  private List<List<TableCell>> singleColumn(String... values) {
    return stream(values)
        .map(value -> List.of(TableCell.builder()
            .header(COLUMN_NAME)
            .combineItem(CombineItem.builder()
                .value(value)
                .build())
            .build()))
        .collect(toList());
  }

  @Test
  @SneakyThrows
  void shouldFilterDistinct() {
    // arrange
    DistinctColumnFilter filter = new DistinctColumnFilter(COLUMN_NAME);
    List<List<TableCell>> column = singleColumn("foo", "bar", "foo", "bar");

    // act
    List<List<TableCell>> filtered = filter.apply(column);

    // assert
    assertThat(filtered).extracting(row -> row.get(0).getValue())
        .containsExactlyInAnyOrder("foo", "bar");
  }

  @Test
  @SneakyThrows
  void shouldNotFilter() {
    // arrange
    DistinctColumnFilter filter = new DistinctColumnFilter(COLUMN_NAME);
    List<List<TableCell>> column = singleColumn("foo", "bar");

    // act
    List<List<TableCell>> filtered = filter.apply(column);

    // assert
    assertThat(filtered).extracting(row -> row.get(0).getValue())
        .containsExactlyInAnyOrder("foo", "bar");
  }
}

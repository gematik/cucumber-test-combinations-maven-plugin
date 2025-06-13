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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.gematik.combine.CombineMojo;
import de.gematik.combine.filter.table.DoubleLineupFilter;
import de.gematik.combine.model.CombineItem;
import de.gematik.combine.model.TableCell;
import java.util.List;
import lombok.SneakyThrows;
import org.apache.maven.plugin.logging.Log;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DoubleLineupFilterTest {

  Log log;

  @BeforeEach
  public void setup() {
    CombineMojo mojo = mock(CombineMojo.class);
    log = mock(Log.class);
    when(mojo.getLog()).thenReturn(log);
    CombineMojo.setInstance(mojo);
  }

  private List<List<TableCell>> table() {
    return List.of(
        List.of(
            TableCell.builder().combineItem(CombineItem.builder().value("A").build()).build(),
            TableCell.builder().combineItem(CombineItem.builder().value("B").build()).build()),
        List.of(
            TableCell.builder().combineItem(CombineItem.builder().value("B").build()).build(),
            TableCell.builder().combineItem(CombineItem.builder().value("A").build()).build()));
  }

  @Test
  @SneakyThrows
  void shouldFilterDoubleLineup() {
    // arrange
    DoubleLineupFilter filter = new DoubleLineupFilter(false);

    // act
    List<List<TableCell>> result = filter.apply(table());

    // assert
    assertThat(result).hasSize(1);
  }

  @Test
  @SneakyThrows
  void shouldNotFilterDoubleLineup() {
    // arrange
    DoubleLineupFilter filter = new DoubleLineupFilter(true);

    // act
    List<List<TableCell>> result = filter.apply(table());

    // assert
    assertThat(result).hasSize(2);
  }
}

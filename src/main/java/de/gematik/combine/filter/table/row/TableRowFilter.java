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

import static de.gematik.combine.CombineMojo.getPluginLog;
import static de.gematik.combine.filter.FilterOrder.ROW_ONLY;
import static java.lang.Math.min;
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

import de.gematik.combine.filter.FilterOrder;
import de.gematik.combine.filter.table.AbstractTableFilter;
import de.gematik.combine.model.TableCell;
import java.util.ArrayList;
import java.util.List;

/**
 * This is an adapter class for TableFilters that operate only on single rows. It implements the
 * iteration over table rows with chunking for tables greater {@link TableRowFilter#CHUNK_SIZE
 * CHUNK_SIZE}.
 */
public abstract class TableRowFilter extends AbstractTableFilter implements RowFilter {

  public static final int CHUNK_SIZE = 1000000;

  @Override
  public List<List<TableCell>> apply(List<List<TableCell>> tableRows) {
    getPluginLog().debug(format("applying %s on %d rows", this, tableRows.size()));
    List<List<TableCell>> result = new ArrayList<>();

    int chunkCount = tableRows.size() / CHUNK_SIZE + 1;
    for (int i = 0; i < tableRows.size(); i += CHUNK_SIZE) {
      List<List<TableCell>> chunk = tableRows.subList(i, min(i + CHUNK_SIZE, tableRows.size()));
      List<List<TableCell>> filteredChunk = chunk.stream().filter(this).collect(toList());
      result.addAll(filteredChunk);
      if (chunkCount > 1) {
        getPluginLog().debug(format("processing chunk %d of %d", i / CHUNK_SIZE + 1, chunkCount));
      }
    }
    return result;
  }

  @Override
  public FilterOrder getFilterOrder() {
    return ROW_ONLY;
  }
}

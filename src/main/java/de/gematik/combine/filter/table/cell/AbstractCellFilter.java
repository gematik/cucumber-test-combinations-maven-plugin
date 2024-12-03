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

package de.gematik.combine.filter.table.cell;

import de.gematik.combine.filter.table.SoftFilter;

/**
 * This class extends all classes that should be {@link
 * de.gematik.combine.filter.table.cell.CellFilter} with property <i>soft</i>, so the {@link
 * de.gematik.combine.filter.table.cell.CellFilter} interface can stay functional interface. All
 * Filters should extend this class and not implement {@link
 * de.gematik.combine.filter.table.cell.CellFilter}
 */
public abstract class AbstractCellFilter implements CellFilter {

  private boolean soft;

  @Override
  public boolean isSoft() {
    return soft;
  }

  @Override
  public SoftFilter setSoft(boolean soft) {
    this.soft = soft;
    return this;
  }
}

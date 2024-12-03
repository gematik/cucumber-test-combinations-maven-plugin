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

package de.gematik.combine.filter.table;

/**
 * This interface is used to set {@link de.gematik.combine.filter.table.TableFilter} and {@link
 * de.gematik.combine.filter.table.cell.CellFilter} as SoftFilter. SoftFilter should be deleted if
 * not enough rows for an example could be generated
 */
public interface SoftFilter {

  default boolean isSoft() {
    return false;
  }

  default SoftFilter setSoft(boolean soft) {
    return this;
  }
}

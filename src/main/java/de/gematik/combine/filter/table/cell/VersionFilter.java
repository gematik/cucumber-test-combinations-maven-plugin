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

package de.gematik.combine.filter.table.cell;

import static de.gematik.combine.model.properties.Version.getSemanticVersion;
import static java.lang.String.format;
import static java.util.Objects.nonNull;

import de.gematik.combine.CombineMojo;
import de.gematik.combine.model.TableCell;
import de.gematik.combine.model.properties.Version;
import de.gematik.combine.util.CompareOperator;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

/**
 * This filter removes cells in which the version property is not set or doesn't match the specified
 * criteria.
 */
@Data
@RequiredArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class VersionFilter implements CellFilter {

  private final CompareOperator operator;
  private final Version filterVersion;

  @Override
  public boolean test(TableCell tableCell) {
    String versionProperty = CombineMojo.getInstance().getVersionProperty();
    if (!tableCell.hasProperty(versionProperty)) {
      CombineMojo.appendError(
          format("version property is missing for -> value: %s%s", tableCell.getValue(),
              nonNull(tableCell.getUrl()) ? format(" url: %s", tableCell.getUrl()) : ""),
          CombineMojo.ErrorType.PROPERTY);
      return false;
    }

    Version itemVersion = getSemanticVersion(tableCell.getProperties().get(versionProperty));
    return operator.includesResultOf(itemVersion.compareTo(filterVersion));
  }

  @Override
  public String toString() {
    return format("%s%s", operator.getLiteral(), filterVersion);
  }

}

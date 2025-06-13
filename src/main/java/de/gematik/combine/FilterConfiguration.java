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

package de.gematik.combine;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.apache.maven.plugins.annotations.Parameter;

@Builder(toBuilder = true)
@Data
@ToString
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class FilterConfiguration {

  /** Decide if a distinct item is allowed to show up twice in one line */
  @Default
  @Parameter(property = "allowSelfCombine", defaultValue = "false")
  private boolean allowSelfCombine = false;

  /**
   * Decide if one combination of items is allowed to show up in different lineups (e.g. | Api1 |
   * Api2 | and | Api2 | Api1 | )
   */
  @Default
  @Parameter(property = "allowDoubleLineup", defaultValue = "false")
  private boolean allowDoubleLineup = false;

  /** global maximum of rows for each table */
  @Default
  @Parameter(property = "maxTableRows", defaultValue = "" + Integer.MAX_VALUE)
  private int maxTableRows = Integer.MAX_VALUE;

  /** shuffles the generated table */
  @Default
  @Parameter(property = "shuffleCombinations", defaultValue = "false")
  private boolean shuffleCombinations = false;

  /** Creates a minimal table */
  @Default
  @Parameter(property = "minimalTable", defaultValue = "false")
  private boolean minimalTable = false;
}

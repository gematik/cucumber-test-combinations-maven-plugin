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

package de.gematik.combine;

import static java.util.Objects.nonNull;

import de.gematik.combine.filter.project.ProjectCellFilter;
import de.gematik.combine.filter.project.ProjectRowFilter;
import de.gematik.combine.tags.parser.VersionFilterParser;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.apache.maven.plugins.annotations.Parameter;

@Builder(toBuilder = true)
@ToString
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class ProjectFilters {

  @Getter
  private final List<ProjectCellFilter> cellFilters = new ArrayList<>();
  @Getter
  private final List<ProjectRowFilter> rowFilters = new ArrayList<>();
  @Parameter(name = "version")
  String version;
  private ProjectCellFilter versionFilter;

  public void parseProjectFilters() {
    if (nonNull(version)) {
      cellFilters.add(VersionFilterParser.parseProjectFilter(version));
    }
  }
}

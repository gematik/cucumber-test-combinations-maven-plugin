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

package de.gematik.combine;

import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Builder(toBuilder = true)
@Getter
@ToString
@EqualsAndHashCode
public class CombineConfiguration {

  private String templateDir;
  private String outputDir;
  private String combineItemFile;
  @Builder.Default
  private String templateFileEnding = ".cute";
  @Builder.Default
  private String pluginTagCategory = "Plugin";
  @Builder.Default
  private String versionFilterTagCategory = "VersionFilter";
  @Builder.Default
  private List<String> emptyExamplesTags = new ArrayList<>();
  @Builder.Default
  private List<String> defaultExamplesTags = new ArrayList<>();
  @Builder.Default
  private List<String> skipTags = new ArrayList<>();
  private FilterConfiguration filterConfiguration;
  private ProjectFilters projectFilters;
  private boolean breakIfTableToSmall;
  private int minTableSize;
  private boolean breakIfMinimalTableError;
  private boolean softFilterToHardFilter;
  private boolean countExecutions;
  private List<String> countExecutionsFormat;

}

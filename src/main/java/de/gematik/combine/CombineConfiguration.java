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

import java.util.ArrayList;
import java.util.List;

import de.gematik.combine.count.ExecutionCounter;
import io.cucumber.core.options.CucumberProperties;
import io.cucumber.core.options.CucumberPropertiesParser;
import io.cucumber.tagexpressions.Expression;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Builder(toBuilder = true)
@Getter
@ToString
@EqualsAndHashCode
public class CombineConfiguration {

  private List<String> templateSources;
  private String outputDir;
  private String combineItemFile;
  @Builder.Default private String templateFileEnding = ".cute";
  @Builder.Default private String pluginTagCategory = "Plugin";
  @Builder.Default private String versionFilterTagCategory = "VersionFilter";
  @Builder.Default private List<String> emptyExamplesTags = new ArrayList<>();
  @Builder.Default private List<String> defaultExamplesTags = new ArrayList<>();
  @Builder.Default private List<String> skipTags = new ArrayList<>();
  private FilterConfiguration filterConfiguration;
  private ProjectFilters projectFilters;
  private boolean breakIfTableToSmall;
  private int minTableSize;
  private boolean breakIfMinimalTableError;
  private boolean softFilterToHardFilter;
  private boolean countExecutions;
  private List<ExecutionCounter.Format> countExecutionsFormat;
  private final List<Expression> filterTagExpressions = new CucumberPropertiesParser().parse(CucumberProperties.create()).build().getTagExpressions();

  public boolean filterTagsMatch(List<String> tags) {
    return filterTagExpressions.stream().allMatch(expression -> expression.evaluate(tags));
  }
}

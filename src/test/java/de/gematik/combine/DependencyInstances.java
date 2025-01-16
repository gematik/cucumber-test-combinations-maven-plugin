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

import de.gematik.combine.execution.ExamplesProcessor;
import de.gematik.combine.execution.FileProcessor;
import de.gematik.combine.execution.GherkinProcessor;
import de.gematik.combine.execution.ScenarioProcessor;
import de.gematik.combine.execution.TableGenerator;
import de.gematik.combine.tags.TagParser;

public class DependencyInstances {
  public static final TableGenerator TABLEGENERATOR = new TableGenerator();
  public static final TagParser TAG_PARSER = new TagParser();
  public static final ExamplesProcessor EXAMPLES_PROCESSOR =
      new ExamplesProcessor(TAG_PARSER, TABLEGENERATOR);
  public static final ScenarioProcessor SCENARIO_PROCESSOR =
      new ScenarioProcessor(EXAMPLES_PROCESSOR);
  public static final GherkinProcessor GHERKIN_PROCESSOR = new GherkinProcessor(SCENARIO_PROCESSOR);
  public static final FileProcessor FILE_PROCESSOR = new FileProcessor(GHERKIN_PROCESSOR);
}

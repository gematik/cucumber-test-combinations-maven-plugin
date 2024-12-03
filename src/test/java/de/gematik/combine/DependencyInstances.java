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

import static de.gematik.combine.tags.parser.AllowDoubleLineupTagParser.ALLOW_DOUBLE_LINEUP_TAG;
import static de.gematik.combine.tags.parser.AllowSelfCombineTagParser.ALLOW_SELF_COMBINE_TAG;
import static de.gematik.combine.tags.parser.DistinctColumnPropertyTagParser.DISTINCT_COLUMN_PROPERTY_TAG;
import static de.gematik.combine.tags.parser.DistinctColumnTagParser.DISTINCT_COLUMN_TAG;
import static de.gematik.combine.tags.parser.DistinctRowPropertyTagParser.DISTINCT_ROW_PROPERTY_TAG;
import static de.gematik.combine.tags.parser.EqualRowPropertyTagParser.EQUAL_ROW_PROPERTY_TAG;
import static de.gematik.combine.tags.parser.FilterTagParser.JEXL_ROW_FILTER_TAG;
import static de.gematik.combine.tags.parser.MaxRowsTagParser.MAX_ROWS_TAG;
import static de.gematik.combine.tags.parser.MaxSameColumnPropertyTagParser.MAX_SAME_COLUMN_PROPERTY_TAG;
import static de.gematik.combine.tags.parser.MinimalTableTagParser.MINIMAL_TABLE_TAG;
import static de.gematik.combine.tags.parser.RequirePropertyTagParser.REQUIRE_PROPERTY_TAG;
import static de.gematik.combine.tags.parser.RequireTagTagParser.REQUIRE_TAG_TAG;
import static de.gematik.combine.tags.parser.ShuffleTagParser.SHUFFLE_TAG;
import static de.gematik.combine.tags.parser.VersionFilterParser.VERSION_TAG;
import static java.util.Map.entry;

import de.gematik.combine.execution.ExamplesProcessor;
import de.gematik.combine.execution.FileProcessor;
import de.gematik.combine.execution.GherkinProcessor;
import de.gematik.combine.execution.ScenarioProcessor;
import de.gematik.combine.execution.TableGenerator;
import de.gematik.combine.tags.SingleTagParser;
import de.gematik.combine.tags.TagParser;
import de.gematik.combine.tags.parser.AllowDoubleLineupTagParser;
import de.gematik.combine.tags.parser.AllowSelfCombineTagParser;
import de.gematik.combine.tags.parser.DistinctColumnPropertyTagParser;
import de.gematik.combine.tags.parser.DistinctColumnTagParser;
import de.gematik.combine.tags.parser.DistinctRowPropertyTagParser;
import de.gematik.combine.tags.parser.EqualRowPropertyTagParser;
import de.gematik.combine.tags.parser.FilterTagParser;
import de.gematik.combine.tags.parser.MaxRowsTagParser;
import de.gematik.combine.tags.parser.MaxSameColumnPropertyTagParser;
import de.gematik.combine.tags.parser.MinimalTableTagParser;
import de.gematik.combine.tags.parser.RequirePropertyTagParser;
import de.gematik.combine.tags.parser.RequireTagTagParser;
import de.gematik.combine.tags.parser.ShuffleTagParser;
import de.gematik.combine.tags.parser.VersionFilterParser;
import java.util.Map;

public class DependencyInstances {

  public static final Map<String, SingleTagParser> ALL_PARSERS =
      Map.ofEntries(
          entry(ALLOW_DOUBLE_LINEUP_TAG, new AllowDoubleLineupTagParser()),
          entry(ALLOW_SELF_COMBINE_TAG, new AllowSelfCombineTagParser()),
          entry(MINIMAL_TABLE_TAG, new MinimalTableTagParser()),
          entry(SHUFFLE_TAG, new ShuffleTagParser()),
          entry(MAX_ROWS_TAG, new MaxRowsTagParser()),
          entry(DISTINCT_ROW_PROPERTY_TAG, new DistinctRowPropertyTagParser()),
          entry(EQUAL_ROW_PROPERTY_TAG, new EqualRowPropertyTagParser()),
          entry(DISTINCT_COLUMN_TAG, new DistinctColumnTagParser()),
          entry(JEXL_ROW_FILTER_TAG, new FilterTagParser()),
          entry(DISTINCT_COLUMN_PROPERTY_TAG, new DistinctColumnPropertyTagParser()),
          entry(MAX_SAME_COLUMN_PROPERTY_TAG, new MaxSameColumnPropertyTagParser()),
          entry(REQUIRE_TAG_TAG, new RequireTagTagParser()),
          entry(REQUIRE_PROPERTY_TAG, new RequirePropertyTagParser()),
          entry(VERSION_TAG, new VersionFilterParser()));

  public static final TableGenerator TABLEGENERATOR = new TableGenerator();
  public static final TagParser TAG_PARSER = new TagParser(ALL_PARSERS);
  public static final ExamplesProcessor EXAMPLES_PROCESSOR =
      new ExamplesProcessor(TAG_PARSER, TABLEGENERATOR);
  public static final ScenarioProcessor SCENARIO_PROCESSOR =
      new ScenarioProcessor(EXAMPLES_PROCESSOR);
  public static final GherkinProcessor GHERKIN_PROCESSOR = new GherkinProcessor(SCENARIO_PROCESSOR);
  public static final FileProcessor FILE_PROCESSOR = new FileProcessor(GHERKIN_PROCESSOR);
}

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

package de.gematik.combine.tags.parser;

import static de.gematik.combine.tags.parser.MaxSameColumnPropertyTagParser.MAX_SAME_COLUMN_PROPERTY_TAG;
import static java.lang.Integer.parseInt;

import de.gematik.combine.filter.table.MaxSameColumnPropertyFilter;
import de.gematik.combine.tags.ParsedTags;
import de.gematik.combine.tags.SingleTagParser;
import de.gematik.combine.tags.TagParser.PreParsedTag;
import javax.inject.Named;
import javax.inject.Singleton;

@Named(MAX_SAME_COLUMN_PROPERTY_TAG)
@Singleton
@SuppressWarnings("unused")
public class MaxSameColumnPropertyTagParser implements SingleTagParser {

  public static final String MAX_SAME_COLUMN_PROPERTY_TAG = "MaxSameColumnProperty";

  @Override
  public void parseTagAndRegister(PreParsedTag preParsedTag, ParsedTags parsedTags) {
    String[] args = preParsedTag.getValue().split(",");
    if (args.length != 3) {
      throw new IllegalArgumentException(
          "'" + preParsedTag.getValue() + "' does not have exact 3 arguments");
    }
    parsedTags.addTableFilter(
        new MaxSameColumnPropertyFilter(args[0], args[1], parseInt(args[2]))
            .setSoft(preParsedTag.isSoft()));
  }
}

/*
 * Copyright 20023 gematik GmbH
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

package de.gematik.combine.tags.parser;

import static de.gematik.combine.tags.parser.RequireTagTagParser.REQUIRE_TAG_TAG;

import de.gematik.combine.filter.table.row.RequireTagRowFilter;
import de.gematik.combine.tags.ParsedTags;
import de.gematik.combine.tags.SingleTagParser;
import de.gematik.combine.tags.TagParser.PreParsedTag;
import javax.inject.Named;
import javax.inject.Singleton;

@Named(REQUIRE_TAG_TAG)
@Singleton
@SuppressWarnings("unused")
public class RequireTagTagParser implements SingleTagParser {

  public static final String REQUIRE_TAG_TAG = "RequireTag";

  @Override
  public void parseTagAndRegister(PreParsedTag preParsedTag, ParsedTags parsedTags) {
    RequireTagRowFilter filter = new RequireTagRowFilter(preParsedTag.getValue());
    filter.setSoft(preParsedTag.isSoft());
    parsedTags.addTableRowFilter(filter);
  }
}

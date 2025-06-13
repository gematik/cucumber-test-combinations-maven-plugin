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

import static java.lang.Boolean.parseBoolean;

import de.gematik.combine.tags.ParsedTags;
import de.gematik.combine.tags.SingleTagParser;
import de.gematik.combine.tags.TagParser.PreParsedTag;
import javax.inject.Named;
import javax.inject.Singleton;

@Named(MinimalTableTagParser.MINIMAL_TABLE_TAG)
@Singleton
@SuppressWarnings("unused")
public class MinimalTableTagParser implements SingleTagParser {

  public static final String MINIMAL_TABLE_TAG = "MinimalTable";

  @Override
  public void parseTagAndRegister(PreParsedTag preParsedTag, ParsedTags parsedTags) {
    boolean minimal = true;
    if (preParsedTag.getValue() != null && !preParsedTag.getValue().isEmpty()) {
      minimal = parseBoolean(preParsedTag.getValue());
    }
    boolean finalMinimal = minimal;
    parsedTags.addConfigModifier(config -> config.toBuilder().minimalTable(finalMinimal).build());
  }
}

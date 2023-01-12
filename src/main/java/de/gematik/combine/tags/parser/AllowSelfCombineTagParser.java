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

package de.gematik.combine.tags.parser;

import static de.gematik.combine.tags.parser.AllowSelfCombineTagParser.ALLOW_SELF_COMBINE_TAG;
import static java.lang.Boolean.parseBoolean;

import de.gematik.combine.tags.ParsedTags;
import de.gematik.combine.tags.SingleTagParser;
import javax.inject.Named;
import javax.inject.Singleton;

@Named(ALLOW_SELF_COMBINE_TAG)
@Singleton
@SuppressWarnings("unused")
public class AllowSelfCombineTagParser implements SingleTagParser {

  public static final String ALLOW_SELF_COMBINE_TAG = "AllowSelfCombine";

  @Override
  public void parseTagAndRegister(String value, ParsedTags parsedTags) {
    boolean allow = true;
    if (value != null && !value.isEmpty()) {
      allow = parseBoolean(value);
    }
    boolean finalAllow = allow;
    parsedTags.addConfigModifier(config -> config.toBuilder()
        .allowSelfCombine(finalAllow)
        .build());

  }
}

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

package de.gematik.combine.tags;

import static de.gematik.combine.CombineMojo.getPluginLog;
import static java.util.regex.Pattern.compile;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.SneakyThrows;
import org.reflections.Reflections;

import javax.inject.Named;

/**
 * This TagParser parses tags in th format `@TagName` or `@TagName(tagValue)`. The parsing of
 * tagValues and the conversion to {@link de.gematik.combine.filter.table.TableFilter TableFilter}s
 * is delegated to {@link SingleTagParser}s.
 */
public class TagParser {

  private static final Pattern tagPattern = compile("@([a-zA-Z0-9_-]+)(?:\\((.*)\\))?");

  private final Map<String, SingleTagParser> tagParsers =
      new Reflections(getClass().getPackageName())
          .getSubTypesOf(SingleTagParser.class).stream()
              .map(TagParser::createParser)
              .collect(Collectors.toMap(TagParser::getParserName, UnaryOperator.identity()));

  @SneakyThrows
  private static SingleTagParser createParser(Class<? extends SingleTagParser> clazz) {
    return clazz.getDeclaredConstructor().newInstance();
  }

  private static String getParserName(SingleTagParser parser) {
    return parser.getClass().getAnnotation(Named.class).value();
  }

  public ParsedTags parseTags(List<String> tags, List<String> columns) {
    ParsedTags tagCollector = new ParsedTags(columns);

    tags.stream()
        .map(this::preParseTag)
        .flatMap(Optional::stream)
        .forEach(tag -> parseTag(tag, tagCollector));

    return tagCollector;
  }

  private void parseTag(PreParsedTag preParsedTag, ParsedTags tagCollector) {
    SingleTagParser tagParser = tagParsers.get(preParsedTag.getTagName());
    if (tagParser == null) {
      getPluginLog().warn("ignoring unknown tag: " + preParsedTag);
      return;
    }
    tagParser.parseTagAndRegister(preParsedTag, tagCollector);
  }

  private Optional<PreParsedTag> preParseTag(String tag) {
    final Matcher matcher = tagPattern.matcher(tag);
    final boolean matches = matcher.matches();

    if (!matches) {
      getPluginLog().warn(tag + " does not match format for tags");
      return Optional.empty();
    }
    final String shortName = matcher.group(1);
    final boolean hasValue = matcher.groupCount() == 2;

    String value = null;
    if (hasValue) {
      value = matcher.group(2);
    }
    return Optional.of(new PreParsedTag(shortName, value));
  }

  @Getter
  public static class PreParsedTag {

    final String tagName;
    final String value;
    final boolean isSoft;

    public PreParsedTag(String tagName, String value) {
      if (tagName.startsWith("Soft")) {
        this.tagName = tagName.substring(4);
        this.isSoft = true;
      } else {
        this.tagName = tagName;
        this.isSoft = false;
      }
      this.value = value;
    }

    @Override
    public String toString() {
      return "@" + tagName + "(" + value + ")";
    }
  }
}

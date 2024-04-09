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

package de.gematik.combine.tags.parser;

import static de.gematik.combine.model.properties.Version.getSemanticVersion;
import static de.gematik.combine.tags.parser.VersionFilterParser.VERSION_TAG;
import static de.gematik.combine.util.CompareOperator.OPERATOR_FORMAT;
import static de.gematik.combine.util.CompareOperator.getUsableOperators;
import static java.lang.String.format;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

import de.gematik.combine.filter.project.ProjectCellFilter;
import de.gematik.combine.filter.project.ProjectVersionFilter;
import de.gematik.combine.filter.table.cell.VersionFilter;
import de.gematik.combine.tags.ParsedTags;
import de.gematik.combine.tags.SingleTagParser;
import de.gematik.combine.tags.TagParser.PreParsedTag;
import de.gematik.combine.util.CompareOperator;
import java.util.List;
import javax.inject.Named;
import javax.inject.Singleton;

@Named(VERSION_TAG)
@Singleton
@SuppressWarnings("unused")
public class VersionFilterParser implements SingleTagParser {

  public static final String VERSION_TAG = "Version";

  public static ProjectCellFilter parseProjectFilter(String val) {
    VersionFilter filter = parseFilterValue(val);
    return new ProjectVersionFilter(filter.getOperator(), filter.getFilterVersion());
  }

  @Override
  public void parseTagAndRegister(PreParsedTag preParsedTag, ParsedTags parsedTags) {
    VersionFilter filter = parseFilterValue(preParsedTag.getValue());
    filter.setSoft(preParsedTag.isSoft());
    List<String> args = checkForOperator(preParsedTag.getValue(), filter.getOperator());
    String[] columns = args.get(0).split(",");
    if (columns.length == 0 || (columns.length == 1 && columns[0].isBlank())) {
      throw new IllegalArgumentException(
          format(
              "%s: '%s' does not have any headers, which are necessary for a version "
                  + "filter that is set to a scenario",
              VERSION_TAG, preParsedTag.getValue()));
    }
    for (String header : columns) {
      parsedTags.addCellFilter(header, filter);
    }
  }

  private static List<String> checkForOperator(String tagVal, CompareOperator op) {
    return stream(checkForOperatorName(tagVal, op))
        .flatMap(s -> stream(checkForOperatorLiteral(s, op)))
        .collect(toList());
  }

  private static String[] checkForOperatorName(String tagVal, CompareOperator op) {
    return tagVal.trim().split(format(OPERATOR_FORMAT, op.name()));
  }

  private static String[] checkForOperatorLiteral(String tagVal, CompareOperator op) {
    return tagVal.trim().split(op.getLiteral());
  }

  private static VersionFilter getVersionFilter(CompareOperator op, String version) {
    return new VersionFilter(op, getSemanticVersion(version));
  }

  private static VersionFilter parseFilterValue(String val) {
    for (CompareOperator op : CompareOperator.values()) {
      List<String> args = checkForOperator(val, op);
      if (args.size() == 1) {
        continue;
      }
      if (args.size() != 2) {
        throw new IllegalArgumentException(
            format("%s: '%s' does not have the right number of arguments", VERSION_TAG, val));
      }
      return getVersionFilter(op, args.get(1));
    }
    throw new IllegalArgumentException(
        format("A version filter must contain a comparison operator: %s", getUsableOperators()));
  }
}

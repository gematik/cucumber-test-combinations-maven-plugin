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

package de.gematik.combine.util;

import static java.lang.String.format;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum CompareOperator {
  EQ("==", List.of(0)),
  NE("!=", List.of(-1, 1)),
  LE("<=", List.of(-1, 0)),
  LT("<", List.of(-1)),
  GE(">=", List.of(0, 1)),
  GT(">", List.of(1));

  public static final String OPERATOR_FORMAT = "--%s--";

  @Getter
  private final String literal;
  private final List<Integer> matchingResults;

  public static String getUsableOperators() {
    return Arrays.stream(CompareOperator.values())
        .map(o -> format(OPERATOR_FORMAT + " / %s", o.name(), o.getLiteral()))
        .collect(Collectors.joining(", "));
  }

  public boolean includesResultOf(Integer comparedToResult) {
    return matchingResults.contains(comparedToResult);
  }

}

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

package de.gematik.prepare.pooling.strategies;

import static de.gematik.prepare.pooling.strategies.MatchStrategy.MATCHING;
import static de.gematik.prepare.pooling.strategies.MatchStrategy.NOT_MATCHING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class RegexStrategyTest {


  private static Stream<Arguments> testAllRegex() {
    return Stream.of(
        arguments(List.of("^a.*"), Set.of("test", "accepted"), MATCHING),
        arguments(List.of("^a.*"), Set.of("best", "of", "the", "world"), NOT_MATCHING),
        arguments(List.of("exactMatch"), Set.of("exactMatch"), MATCHING),
        arguments(List.of("ExactMatch"),  Set.of("exactMatch"), NOT_MATCHING),
        arguments(List.of(), Set.of("bas"), NOT_MATCHING)
    );
  }

  @MethodSource
  @ParameterizedTest
  void testAllRegex(List<String> groups,  Set<String> itemGroups, String result) {
    RegexStrategy underTest = new RegexStrategy(groups);
    assertThat(underTest.match(itemGroups)).isEqualTo(result);
  }
}
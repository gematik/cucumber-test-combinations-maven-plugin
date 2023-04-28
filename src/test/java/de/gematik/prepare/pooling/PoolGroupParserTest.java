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

package de.gematik.prepare.pooling;

import static de.gematik.prepare.pooling.GroupMatchStrategyType.CASE_SENSITIVE;
import static de.gematik.prepare.pooling.GroupMatchStrategyType.REGEX;
import static de.gematik.prepare.pooling.GroupMatchStrategyType.WILDCARD;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

class PoolGroupParserTest {

  PoolGroupParser underTest = new PoolGroupParser();

  @Test
  @SneakyThrows
  void onlyOneGroup() {
    List<PoolGroup> res = underTest.transformStringToGroups("oneGroup");
    assertThat(res).hasSize(1);
    assertThat(res.get(0).getGroupPattern()).hasSize(1);
  }

  @Test
  @SneakyThrows
  void twoGroups() {
    List<PoolGroup> res = underTest.transformStringToGroups("oneGroup;secondGroup");
    assertThat(res).hasSize(2);
    assertThat(res.get(0).getGroupPattern()).hasSize(1);
    assertThat(res.get(1).getGroupPattern()).hasSize(1);
  }

  @Test
  @SneakyThrows
  void twoGroupsOneWithAmount() {
    List<PoolGroup> res = underTest.transformStringToGroups("oneGroup,4;secondGroup");
    assertThat(res).hasSize(2);
    assertThat(res.get(0).getGroupPattern()).hasSize(1);
    assertThat(res.get(0).getAmount()).isEqualTo(4);
    assertThat(res.get(1).getGroupPattern()).hasSize(1);
  }

  @Test
  @SneakyThrows
  void twoGroupsOneWithoutAmountButOtherStrategy() {
    List<PoolGroup> res = underTest.transformStringToGroups("oneGroup,,REGEX;secondGroup");
    assertThat(res).hasSize(2);
    assertThat(res.get(0).getGroupPattern()).hasSize(1);
    assertThat(res.get(0).getStrategy()).isEqualTo(REGEX);
    assertThat(res.get(1).getGroupPattern()).hasSize(1);
  }

  @Test
  @SneakyThrows
  void oneGroupWithMultiplePatterns() {
    List<PoolGroup> res = underTest.transformStringToGroups("oneGroup|twoGroup|thirdGroup");
    assertThat(res).hasSize(1);
    assertThat(res.get(0).getGroupPattern()).hasSize(3);
  }

  @Test
  @SneakyThrows
  void multipleGroupsWithMultiplePatternsAndAmounts() {
    List<PoolGroup> res = underTest.transformStringToGroups(
        "oneGroup|twoGroup|thirdGroup,4,WILDCARD;oneGroup|twoGroup,2,CASE_SENSITIVE;oneGroup,3,REGEX");
    assertThat(res).hasSize(3);
    assertThat(res.get(0).getGroupPattern()).hasSize(3);
    assertThat(res.get(1).getGroupPattern()).hasSize(2);
    assertThat(res.get(2).getGroupPattern()).hasSize(1);
    assertThat(res.get(0).getAmount()).isEqualTo(4);
    assertThat(res.get(1).getAmount()).isEqualTo(2);
    assertThat(res.get(2).getAmount()).isEqualTo(3);
    assertThat(res.get(0).getStrategy()).isEqualTo(WILDCARD);
    assertThat(res.get(1).getStrategy()).isEqualTo(CASE_SENSITIVE);
    assertThat(res.get(2).getStrategy()).isEqualTo(REGEX);
  }

  @Test
  @SneakyThrows
  void shouldThrowExceptionIfStrategyUnknown() {
    assertThatThrownBy(() -> underTest.transformStringToGroups("oneGroup,,UNKNOWN"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("No enum constant de.gematik.prepare.pooling.GroupMatchStrategyType.UNKNOWN");
  }
}
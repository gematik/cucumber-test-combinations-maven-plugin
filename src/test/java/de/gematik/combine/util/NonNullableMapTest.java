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

package de.gematik.combine.util;

import static de.gematik.combine.util.NonNullableMap.nonNullableMap;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.Test;

class NonNullableMapTest {

  private static final int EXISTING_KEY = 1;
  private static final int VALUE = 4711;
  private static final int DEFAULT_VALUE = 42;
  private static final int NON_EXISTING_KEY = 99;
  private static final Map<Integer, Integer> INNER_MAP = Map.of(EXISTING_KEY, VALUE);
  private final NonNullableMap<Integer, Integer> testSubject = nonNullableMap(INNER_MAP,
      key -> DEFAULT_VALUE);

  @Test
  @SuppressWarnings("java:S5838")
  void getDefaultValue() {
    assertThat(testSubject).doesNotContainKey(NON_EXISTING_KEY);
    assertThat(testSubject.get(NON_EXISTING_KEY))
        .isEqualTo(DEFAULT_VALUE);
  }

  @Test
  @SuppressWarnings("java:S5838")
  void getExistingValue() {
    assertThat(testSubject).containsKey(EXISTING_KEY);
    assertThat(testSubject.get(EXISTING_KEY))
        .isEqualTo(VALUE);
  }

  @Test
  void size() {
    assertThat(testSubject).hasSameSizeAs(INNER_MAP);
  }

  @Test
  void isEmpty() {
    assertThat(testSubject.isEmpty())
        .isEqualTo(INNER_MAP.isEmpty());
  }

  @Test
  void containsKey() {
    assertThat(testSubject.containsKey(1))
        .isEqualTo(INNER_MAP.containsKey(1));
  }

  @Test
  void containsValue() {
    assertThat(testSubject.containsValue(1))
        .isEqualTo(INNER_MAP.containsValue(1));
  }

  @Test
  void keySet() {
    assertThat(testSubject.keySet())
        .isEqualTo(INNER_MAP.keySet());
  }

  @Test
  void values() {
    assertThat(testSubject.values())
        .isEqualTo(INNER_MAP.values());
  }

  @Test
  void entrySet() {
    assertThat(testSubject.entrySet())
        .isEqualTo(INNER_MAP.entrySet());
  }
}
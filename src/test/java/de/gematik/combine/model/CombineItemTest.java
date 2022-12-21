/*
 * Copyright (c) 2022 gematik GmbH
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

package de.gematik.combine.model;

import static de.gematik.utils.MockPluginLog.withMockedPluginLog;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.Mockito.verify;

import de.gematik.utils.MockPluginLog;
import java.util.Map;
import org.junit.jupiter.api.Test;

class CombineItemTest {

  private static final String KEY = "NOT_HERE";
  private static final String VALUE = "ITEM";
  private final CombineItem noPropertiesItem =
      CombineItem.builder().value(VALUE).properties(Map.of()).build();
  private final CombineItem secondItem =
      CombineItem.builder().value(VALUE).build();

  @Test
  void gettingNonExistingPropertyShouldNotThrowException() {
    assertThatNoException().isThrownBy(
        () -> withMockedPluginLog(
            () -> noPropertiesItem.getProperties().get(KEY)));
    verify(MockPluginLog.getMock())
        .info(String.format("item %s does not have property %s", VALUE, KEY));
  }

  @Test
  void itemsWithEqualNamesShouldBeEqual() {
    assertThat(secondItem).isEqualTo(noPropertiesItem);
  }
}

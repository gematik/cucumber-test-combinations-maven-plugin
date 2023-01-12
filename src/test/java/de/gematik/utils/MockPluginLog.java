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

package de.gematik.utils;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

import de.gematik.combine.CombineMojo;
import java.util.function.Supplier;
import lombok.Getter;
import org.apache.maven.plugin.logging.Log;
import org.mockito.MockedStatic;

public class MockPluginLog {

  @Getter
  private static Log mock;

  public static <T> T withMockedPluginLog(Supplier<T> test) {
    mock = mock(Log.class);
    return withMockedPluginLog(test, mock);
  }

  public static <T> T withMockedPluginLog(Supplier<T> test, Log log) {
    try (MockedStatic<CombineMojo> mockedStatic = mockStatic(CombineMojo.class)) {
      mockedStatic.when(CombineMojo::getPluginLog).thenReturn(log);
      return test.get();
    }
  }

}

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

import static de.gematik.utils.Utils.getItemsToCombine;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import de.gematik.combine.CombineMojo;
import de.gematik.combine.model.CombineItem;
import java.io.File;
import java.util.List;
import lombok.SneakyThrows;
import org.apache.maven.plugin.logging.Log;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UtilsTest {

  public static final String URL_VALUE_JSON = "src/test/resources/input/inputValueAndUrl.json";

  CombineMojo mojo;
  Log log;

  @BeforeEach
  @SneakyThrows
  void setup() {
    mojo = spy(new CombineMojo(null));
    log = mock(Log.class);
    lenient().when(mojo.getLog()).thenReturn(log);
  }

  @Test
  @SneakyThrows
  void shouldKeepValue() {
    File f = new File(URL_VALUE_JSON);
    List<CombineItem> combines = getItemsToCombine(f, mojo, false);
    System.out.println(combines);
    combines.forEach(c -> assertThat(c.getValue()).startsWith("API-"));
  }

}

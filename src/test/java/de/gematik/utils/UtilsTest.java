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

package de.gematik.utils;

import static de.gematik.utils.Utils.getItemsToCombine;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import de.gematik.combine.CombineMojo;
import de.gematik.combine.model.CombineItem;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.logging.Log;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

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
    List<CombineItem> combines = getItemsToCombine(f, mojo);
    combines.forEach(c -> assertThat(c.getValue()).startsWith("API-"));
  }

  @Test
  @SneakyThrows
  void shouldPreferGeneratedFileIfPresent(@TempDir File generatedDir) {
    File f = new File(URL_VALUE_JSON);
    FileUtils.writeStringToFile(
        new File(generatedDir, f.getName()),
        "[{\"value\": \"GENERATED\"}]",
        StandardCharsets.UTF_8);
    List<CombineItem> combines = getItemsToCombine(f, mojo, generatedDir);
    assertThat(combines).hasSize(1);
    assertThat(combines.getFirst().getValue()).isEqualTo("GENERATED");
  }

  @Test
  @SneakyThrows
  void shouldFallBackToSourceFileIfNoGeneratedFile(@TempDir File generatedDir) {
    List<CombineItem> combines = getItemsToCombine(new File(URL_VALUE_JSON), mojo, generatedDir);
    combines.forEach(c -> assertThat(c.getValue()).startsWith("API-"));
  }
}

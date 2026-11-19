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

package de.gematik.combine.e2e;

import static de.gematik.combine.count.ExecutionCounter.COUNT_EXECUTION_FILE_NAME;
import static de.gematik.utils.Utils.ERROR_LOG_FILE_NAME;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.quality.Strictness.LENIENT;

import java.io.File;
import java.util.List;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.junit.jupiter.MockitoSettings;

@MockitoSettings(strictness = LENIENT)
class CustomBuildDirectoryTest extends AbstractCombineMojoTest {

  private static final File DEFAULT_GENERATED_DIR = new File("target", "generated-combine");

  @TempDir File tempDir;

  File generatedDir;

  @SneakyThrows
  @BeforeEach
  void init() {
    FileUtils.deleteDirectory(DEFAULT_GENERATED_DIR);
    File buildDirectory = new File(tempDir, "targetCustom");
    combineMojo.setBuildDirectory(buildDirectory);
    generatedDir = new File(buildDirectory, "generated-combine");
    combineMojo.setMinTableSize(2);
    combineMojo.setCountExecutionsFormat(List.of("json"));
  }

  @Test
  @SneakyThrows
  void shouldWriteGeneratedFilesToCustomBuildDirectory() {
    combineMojo.execute();

    assertThat(combineMojo.getGeneratedCombineItemsDir()).isEqualTo(generatedDir);
    assertThat(new File(generatedDir, COUNT_EXECUTION_FILE_NAME + ".json")).isFile();
    assertThat(new File(generatedDir, ERROR_LOG_FILE_NAME)).isFile();
    assertThat(DEFAULT_GENERATED_DIR).doesNotExist();
  }

  @Test
  @SneakyThrows
  void shouldReadGeneratedCombineItemsFromCustomBuildDirectory() {
    FileUtils.writeStringToFile(
        new File(generatedDir, new File(combineItemsFile()).getName()),
        """
        [
          {"value": "GeneratedApi1", "properties": {"homeserver": "X"}},
          {"value": "GeneratedApi2", "properties": {"homeserver": "X"}}
        ]
        """,
        UTF_8);

    combineMojo.execute();

    String feature = readFile("errorLogTest");
    assertThat(feature).contains("GeneratedApi1").doesNotContain("Api3");
    assertThat(DEFAULT_GENERATED_DIR).doesNotExist();
  }

  @Override
  protected String inputDir() {
    return "./src/test/resources/templates/ErrorLogTest";
  }

  @Override
  protected String combineItemsFile() {
    return "./src/test/resources/input/errorLogTest.json";
  }

  @Override
  protected boolean countExecutions() {
    return true;
  }
}

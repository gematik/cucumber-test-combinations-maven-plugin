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

package de.gematik.combine.e2e;

import static de.gematik.prepare.PrepareItemsMojo.GENERATED_COMBINE_ITEMS_DIR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.quality.Strictness.LENIENT;

import de.gematik.combine.CombineMojo;
import java.io.File;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;

@MockitoSettings(strictness = LENIENT)
class ErrorLogTest extends AbstractCombineMojoTest {

  @SneakyThrows
  @BeforeEach
  void init() {
    FileUtils.deleteDirectory(new File(GENERATED_COMBINE_ITEMS_DIR));
    combineMojo.setMinTableSize(2);
  }

  @AfterEach
  void delete() {
    combineMojo.setBreakIfTableToSmall(false);
    combineMojo.setMinTableSize(1000000);
  }

  @Test
  @SneakyThrows
  void errorLogShouldBeFilled() {
    combineMojo.execute();
    assertThat(CombineMojo.getTableSizeErrorLog()).hasSize(2);
  }

  @Test
  @SneakyThrows
  void shouldThrowExceptionIf() {
    combineMojo.setBreakIfTableToSmall(true);
    assertThatThrownBy(() -> combineMojo.execute())
        .isInstanceOf(MojoExecutionException.class)
        .hasMessageStartingWith("Scenarios with insufficient examples found ->");
  }

  @Test
  @SneakyThrows
  void shouldCreateFile() {
    combineMojo.setBreakIfTableToSmall(true);
    assertThatThrownBy(() -> combineMojo.execute())
        .isInstanceOf(MojoExecutionException.class)
        .hasMessageStartingWith("Scenarios with insufficient examples found ->");
  }

  @Test
  @SneakyThrows
  void shouldConsiderMinimalTableSizeSetToOne() {
    combineMojo.setMinTableSize(1);
    combineMojo.execute();
    assertThat(CombineMojo.getTableSizeErrorLog()).hasSize(1);
  }

  @Test
  @SneakyThrows
  void shouldConsiderMinimalTableSizeSetToZero() {
    combineMojo.setMinTableSize(0);
    combineMojo.execute();
    assertThat(CombineMojo.getTableSizeErrorLog()).isEmpty();
  }

  @Override
  protected String combineItemsFile() {
    return "./src/test/resources/input/errorLogTest.json";
  }

}

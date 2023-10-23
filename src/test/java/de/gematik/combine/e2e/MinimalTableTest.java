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

import de.gematik.combine.CombineMojo;
import lombok.SneakyThrows;
import org.apache.maven.plugin.MojoExecutionException;
import org.junit.jupiter.api.Test;

import java.util.stream.IntStream;

import static de.gematik.combine.CombineMojo.MINIMAL_TABLE_ERROR_HEADER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MinimalTableTest extends AbstractCombineMojoTest {

  private final int TESTCASES_IN_FILE = 2;

  @Test
  @SneakyThrows
  void shouldNotThrowException() {
    combineMojo.setBreakIfTableToSmall(true);
    int retries = 20;
    IntStream.range(0, retries).forEach(i -> combineMojo.execute());
    assertThat(CombineMojo.getMinimalTableErrorLog()).hasSize(retries * TESTCASES_IN_FILE);
  }

  @Test
  @SneakyThrows
  void shouldThrowExceptionIfBreakMinimalTableTrue() {
    combineMojo.setBreakIfMinimalTableError(true);
    assertThatThrownBy(() -> combineMojo.execute())
        .isInstanceOf(MojoExecutionException.class)
        .hasMessageStartingWith(MINIMAL_TABLE_ERROR_HEADER);
    assertThat(CombineMojo.getMinimalTableErrorLog()).hasSize(TESTCASES_IN_FILE);
  }

  @Override
  protected String combineItemsFile() {
    return "./src/test/resources/input/minimalTable.json";
  }

}

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

package de.gematik.combine.execution;

import de.gematik.combine.CombineConfiguration;
import de.gematik.combine.CombineMojo;
import io.cucumber.messages.types.GherkinDocument;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.logging.Log;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static de.gematik.combine.execution.FileProcessor.parseGherkinString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GherkinProcessorTest {

  private Log log;
  private ScenarioProcessor sp;
  CombineConfiguration config =
      CombineConfiguration.builder()
          .skipTags(List.of("@wip"))
          .breakIfTableToSmall(false)
          .minTableSize(1)
          .breakIfMinimalTableError(false)
          .softFilterToHardFilter(false)
          .countExecutions(false)
          .build();

  @BeforeEach
  public void setup() {
    CombineMojo mojo = mock(CombineMojo.class);
    log = mock(Log.class);
    sp = mock(ScenarioProcessor.class);
    when(mojo.getLog()).thenReturn(log);
    CombineMojo.setInstance(mojo);
  }

  @Test
  @SneakyThrows
  void shouldNotCallMethodProcessIfSkipAnnotationOnFile() {
    // arrange
    File f =
        new File(
            "./src/test/resources/combine/execution/FileProcessorTest/WithAnnotationAboveFile.feature.cute");
    GherkinDocument gd = parseGherkinString(f.toURI().toString(), FileUtils.readFileToString(f, StandardCharsets.UTF_8));
    GherkinProcessor gp = new GherkinProcessor(sp);
    // act
    gp.generateExamples(gd, config, null);
    // assert
    verify(sp, times(0)).generateExamples(any(), any(), any());
  }

  @Test
  @SneakyThrows
  void shouldCallMethodProcessIfSkipAnnotationOnlyAboveOneSzenario() {
    // arrange
    File f =
        new File(
            "./src/test/resources/combine/execution/FileProcessorTest/WithAnnotationAboveSzenario.feature.cute");
    GherkinDocument gd = parseGherkinString(f.toURI().toString(), FileUtils.readFileToString(f, StandardCharsets.UTF_8));
    GherkinProcessor gp = new GherkinProcessor(sp);
    // act
    gp.generateExamples(gd, config, null);
    // assert
    verify(sp, times(1)).generateExamples(any(), any(), any());
  }
}

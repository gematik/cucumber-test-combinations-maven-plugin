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

package de.gematik.combine.e2e;

import static de.gematik.combine.DependencyInstances.FILE_PROCESSOR;
import static java.nio.file.Files.readAllBytes;
import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import de.gematik.combine.CombineMojo;
import de.gematik.combine.FilterConfiguration;
import java.io.File;
import java.util.Arrays;
import lombok.SneakyThrows;
import org.apache.maven.plugin.logging.Log;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public abstract class AbstractCombineMojoTest {

  public static final String WITHOUT_FILTERS_FILE_ENDING = ".withoutFilters";

  @Mock
  protected Log log;

  protected CombineMojo combineMojo;

  @BeforeEach
  public void initCombineMojo() {
    combineMojo = spy(new CombineMojo(FILE_PROCESSOR));
    combineMojo.setCombineItemsFile(combineItemsFile());
    combineMojo.setTemplateDir(inputDir());
    combineMojo.setOutputDir(outputDir());
    combineMojo.setDefaultExamplesTags(emptyList());
    combineMojo.setEmptyExamplesTags(emptyList());
    combineMojo.setSkipTags(emptyList());
    combineMojo.setEnding(".cute");
    combineMojo.setFilterConfiguration(new FilterConfiguration());
    combineMojo.setMinTableSize(minimalTableSize());
    combineMojo.setBreakIfTableToSmall(breakIfTableToSmall());

    when(combineMojo.getLog()).thenReturn(log);
  }

  @SneakyThrows
  String readFile(String name) {
    File f = Arrays.stream(requireNonNull(new File(outputDir()).listFiles()))
        .filter(file -> file.getName().equals(name))
        .findAny()
        .orElseThrow();
    String renderedString = new String(readAllBytes(f.toPath()));
    return renderedString.replace(" ", "");
  }

  protected String outputDir() {
    return "./target/test/" + getClass().getSimpleName();
  }

  protected String inputDir() {
    return "./src/test/resources/templates/" + getClass().getSimpleName();
  }

  protected String combineItemsFile() {
    return "./src/test/resources/input/input1.json";
  }

  private int minimalTableSize() {
    return 1;
  }

  private boolean breakIfTableToSmall() {
    return false;
  }

}

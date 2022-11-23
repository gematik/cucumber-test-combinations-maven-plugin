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

package de.gematik.utils;

import static de.gematik.prepare.PrepareItemsMojo.GENERATED_COMBINE_ITEMS_DIR;
import static java.lang.String.format;
import static lombok.AccessLevel.PRIVATE;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.gematik.combine.model.CombineItem;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.apache.maven.plugin.Mojo;
import org.apache.maven.plugin.MojoExecutionException;

@NoArgsConstructor(access = PRIVATE)
public class Utils {

  /**
   * read the combine_items.json file into the combineItems list
   *
   * @return list of items to combine
   */
  @SneakyThrows
  public static List<CombineItem> getItemsToCombine(File file, Mojo mojo, boolean useCreated) {
    final BufferedReader br;
    final ObjectMapper mapper = new ObjectMapper();
    final List<CombineItem> combineItems;
    File generatedFile = new File(GENERATED_COMBINE_ITEMS_DIR + File.separator + file.getName());
    if (useCreated && generatedFile.exists()) {
      file = generatedFile;
    }
    mojo.getLog()
        .info(format("Fetching all items from input file '%s'", file.getAbsolutePath()));
    try {
      br = new BufferedReader(new FileReader(file));
    } catch (FileNotFoundException e) {
      mojo.getLog().error(e.getMessage());
      throw new MojoExecutionException(
          format("Can not find combine items file: '%s'", file.getAbsolutePath()), e);
    }
    try {
      combineItems = Arrays.asList(mapper.readValue(br, CombineItem[].class));
    } catch (JsonMappingException | JsonParseException e) {
      mojo.getLog().error(e.getMessage());
      throw new MojoExecutionException(format("JSON could not be properly processed -> %s: %s",
          file.getName(), e.getOriginalMessage()));
    }
    return new ArrayList<>(combineItems);
  }
}

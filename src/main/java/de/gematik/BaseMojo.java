/*
 * Copyright 20023 gematik GmbH
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

package de.gematik;

import static de.gematik.combine.CombineMojo.TEST_RESOURCES_DIR;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Parameter;

@Getter
@Setter
public abstract class BaseMojo extends AbstractMojo {

  /**
   * Path to the directory where the combined items get stored
   */
  public static final String GENERATED_COMBINE_ITEMS_DIR =
      "." + File.separator + "target" + File.separator + "generated-combine";
  private static final List<String> apiErrors = new ArrayList<>();

  /**
   * Path to file that contains the values to combine
   */
  @Parameter(property = "combineItemsFile", defaultValue = TEST_RESOURCES_DIR
      + "combine_items.json")
  String combineItemsFile;
  /**
   * Path to truststore
   */
  @Parameter(property = "truststore")
  String truststore;
  /**
   * Path to truststore
   */
  @Parameter(property = "truststorePw")
  String truststorePw;
  /**
   * Path to truststore
   */
  @Parameter(property = "clientCertStore")
  String clientCertStore;
  /**
   * Path to truststore
   */
  @Parameter(property = "clientCertStorePw")
  String clientCertStorePw;

}

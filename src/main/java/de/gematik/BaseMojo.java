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

package de.gematik;

import lombok.Getter;
import lombok.Setter;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static de.gematik.combine.CombineMojo.TEST_RESOURCES_DIR;

@Getter
@Setter
public abstract class BaseMojo extends AbstractMojo {

  /**
   * Path to the directory where the combined items get stored
   */
  public static final String GENERATED_COMBINE_ITEMS_DIR =
      "." + File.separator + "target" + File.separator + "generated-combine";
  protected final List<String> apiErrors = new ArrayList<>();

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
  /**
   * Host to use for proxy
   */
  @Parameter(property = "proxyHost")
  String proxyHost;
  /**
   * Port to use for proxy
   */
  @Parameter(property = "proxyPort")
  Integer proxyPort;
  /**
   * Parameter to decide if plugin should be executed. All executions are skipped
   */
  @Parameter(property = "skip", defaultValue = "false")
  boolean skip;
  /**
   * Decide if run should break on one or more failed requests. If set to false, will generate a
   * combine_items.json file with responding APIs only (if any).
   */
  @Parameter(name = "breakOnFailedRequest", defaultValue = "true")
  boolean breakOnFailedRequest;
  /**
   * Decide if the build should break
   * if at least one manual set Tag/Property does not match the found
   * value on info endpoint.
   */
  @Parameter(property = "breakOnContextError", defaultValue = "true")
  boolean breakOnContextError;

}

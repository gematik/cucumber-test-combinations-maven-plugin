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

package de.gematik.prepare;

import static de.gematik.combine.CombineMojo.TEST_RESOURCES_DIR;
import static de.gematik.utils.Utils.getItemsToCombine;
import static de.gematik.utils.Utils.writeErrors;
import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;

import de.gematik.combine.model.CombineItem;
import de.gematik.prepare.request.ApiRequester;
import io.cucumber.core.internal.com.fasterxml.jackson.core.JsonProcessingException;
import io.cucumber.core.internal.com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import javax.inject.Inject;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Plugin for filling empty gherkin tables with generated combinations
 */
@Mojo(name = "prepare-items", defaultPhase = LifecyclePhase.GENERATE_TEST_SOURCES)
@Setter
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class PrepareItemsMojo extends AbstractMojo {

  public static final String WARN_MESSAGE = "=== Caution!!! The generated file have modified your input file significantly! ===";
  /**
   * Path to the directory where the combined items get stored
   */
  public static final String GENERATED_COMBINE_ITEMS_DIR =
      "target" + File.separator + "generated-combine";
  private static final List<String> apiErrors = new ArrayList<>();
  @Getter
  @Setter
  private static PrepareItemsMojo instance;
  private final ApiRequester apiRequester;
  /**
   * Path to file that contains the values to combine
   */
  @Parameter(property = "combineItemsFile", defaultValue = TEST_RESOURCES_DIR
      + "combine_items.json")
  String combineItemsFile;
  /**
   * Location to info
   */
  @Parameter(property = "infoResourceLocation")
  String infoResourceLocation;
  /**
   * Expression and tag to set if expression is true
   */
  @Parameter(property = "tagExpressions")
  List<TagExpression> tagExpressions;
  /**
   * List of Expressions that set as property
   */
  @Parameter(property = "propertyExpressions")
  List<PropertyExpression> propertyExpressions;
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
   * Say if the build should break if at least one API is not reachable. If set to false it will
   * generate a combine_items.json file without the not reachable API`s
   */
  @Parameter(property = "hardFail", defaultValue = "true")
  boolean hardFail;
  /**
   * Say if the build should break if at least one manual set Tag/Property does not match the found
   * value on info endpoint.
   */
  @Parameter(property = "configFail", defaultValue = "true")
  boolean configFail;
  private ItemsCreator itemsCreator;
  private List<CombineItem> items;

  public static Log getPluginLog() {
    return instance.getLog();
  }

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    setInstance(this);
    checkExpressionSetCorrectly();
    getLog().info("Going to preprocess " + combineItemsFile);
    itemsCreator = new ItemsCreator(getCreateItemsConfig());
    items = getItemsToCombine(new File(combineItemsFile), getInstance(), false);
    apiRequester.setupTls(truststore, truststorePw, clientCertStore, clientCertStorePw);
    run();
  }

  protected void checkExpressionSetCorrectly() throws MojoExecutionException {
    Optional<TagExpression> checkTag = tagExpressions.stream()
        .filter(t -> t.getExpression() == null || t.getTag() == null).findAny();
    if (checkTag.isPresent()) {
      throw new MojoExecutionException(format("Erroneous configuration: missing %s in %s",
          checkTag.get().getExpression() == null ? "expression" : "tag", checkTag.get()));
    }
    Optional<PropertyExpression> checkProperty = propertyExpressions.stream()
        .filter(t -> t.getExpression() == null || t.getProperty() == null).findAny();
    if (checkProperty.isPresent()) {
      throw new MojoExecutionException(format("Erroneous configuration: missing %s in %s",
          checkProperty.get().getExpression() == null ? "expression" : "property",
          checkProperty.get()));
    }
  }

  protected void run() throws MojoExecutionException {
    List<CombineItem> processedItems = items.stream()
        .map(this::processItem)
        .filter(Objects::nonNull)
        .collect(toList());
    writeErrors(apiErrors, WARN_MESSAGE, false);
    writeErrors(itemsCreator.getConfigErrors());
    if (hardFail && !apiErrors.isEmpty()) {
      throw new MojoExecutionException(
          "Error occurred for following API`s ->\n" + String.join("\n", apiErrors));
    }
    if (configFail && !itemsCreator.getConfigErrors().isEmpty()) {
      throw new MojoExecutionException(
          "Different tags or properties where found ->\n" + String.join("\n",
              itemsCreator.getConfigErrors()));
    }
    writeItemsToFile(processedItems);
  }

  private CombineItem processItem(CombineItem item) {
    String url = item.getUrl() == null ? item.getValue() : item.getUrl();
    try {
      getLog().info("Connecting to " + url);
      Map<?, ?> apiInfo = getApiInfo(
          nonNull(infoResourceLocation) ? url + infoResourceLocation : url);
      itemsCreator.evaluateExpressions(item, apiInfo);
      return item;
    } catch (MojoExecutionException ex) {
      apiErrors.add(item.getValue() + (nonNull(item.getUrl()) ? " -> " + item.getUrl() : "")
          + " -> not reachable");
      getLog().error("Could not connect to api: " + item.getValue() + (nonNull(item.getUrl()) ?
          " url: " + item.getUrl() : ""));
    } catch (JsonProcessingException ex) {
      apiErrors.add(url + " -> could not parse JSON");
      getLog().error("Could not parse JSON from " + url, ex);
    }
    return null;
  }

  private Map<?, ?> getApiInfo(String url) throws MojoExecutionException, JsonProcessingException {
    String apiResponse = apiRequester.getApiResponse(url);
    return new ObjectMapper().readValue(apiResponse, Map.class);
  }

  @SneakyThrows
  private void writeItemsToFile(List<CombineItem> items) {
    String fileName =
        GENERATED_COMBINE_ITEMS_DIR + File.separator + new File(combineItemsFile).getName();
    getLog().info("Created new combine item file -> " + fileName);
    FileUtils.writeStringToFile(
        new File(fileName),
        new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(items), UTF_8);
  }

  private PrepareItemsConfig getCreateItemsConfig() {
    return PrepareItemsConfig.builder()
        .combineItemsFile(combineItemsFile)
        .infoResourceLocation(infoResourceLocation)
        .tagExpressions(tagExpressions)
        .propertyExpressions(propertyExpressions)
        .build();
  }
}

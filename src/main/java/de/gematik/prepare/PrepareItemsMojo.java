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

package de.gematik.prepare;

import static de.gematik.combine.CombineMojo.TEST_RESOURCES_DIR;
import static de.gematik.utils.Utils.getItemsToCombine;
import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.toList;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.gematik.combine.model.CombineItem;
import de.gematik.prepare.request.ApiRequester;
import java.io.File;
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
   * Path to the directory where the combined items get stored
   */
  public static final String GENERATED_COMBINE_ITEMS_DIR =
      "target" + File.separator + "generated-combine";

  @Getter
  @Setter
  private static PrepareItemsMojo instance;

  private final ApiRequester apiRequester;
  private ItemsCreator itemsCreator;
  private List<CombineItem> items;

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

  protected void run() {
    List<CombineItem> processedItems = items.stream()
        .map(this::processItem)
        .filter(Objects::nonNull)
        .collect(toList());

    writeItemsToFile(processedItems);
  }

  private CombineItem processItem(CombineItem item) {
    try {
      getLog().info("Connecting to " + item.getValue());
      Map<?, ?> apiInfo = getApiInfo(item.getValue());
      itemsCreator.evaluateExpressions(item, apiInfo);
      return item;
    } catch (MojoExecutionException ex) {
      getLog().error("Could not connect to " + item.getValue());
    } catch (JsonProcessingException ex) {
      getLog().error("Could not parse JSON from " + item.getValue(), ex);
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

  public static Log getPluginLog() {
    return instance.getLog();
  }
}

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

package de.gematik.prepare;

import static de.gematik.utils.Utils.getItemAsString;
import static de.gematik.utils.Utils.writeErrors;
import static java.lang.String.format;
import static java.lang.String.join;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;

import de.gematik.BaseMojo;
import de.gematik.combine.model.CombineItem;
import de.gematik.prepare.pooling.GroupMatchStrategyType;
import de.gematik.prepare.pooling.PoolGroup;
import de.gematik.prepare.pooling.PoolGroupParser;
import de.gematik.prepare.pooling.Pooler;
import de.gematik.utils.request.ApiRequester;
import io.cucumber.core.internal.com.fasterxml.jackson.core.JsonProcessingException;
import io.cucumber.core.internal.com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.json.JSONObject;

import javax.inject.Inject;
import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Plugin for filling empty gherkin tables with generated combinations
 */
@Mojo(name = "prepare-items", defaultPhase = LifecyclePhase.GENERATE_TEST_SOURCES)
@Setter
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class PrepareItemsMojo extends BaseMojo {

  public static final String FAILED_REQ_WARN_MESSAGE =
      "=== Caution!!! The generated file contains less items than your input! ==="
          + "\n             === This is because of the following errors: ===";
  public static final String CONFIG_FAIL_WARN_MESSAGE =
      "=== Caution!!! The generated file is significantly different from your input! ==="
          + "\n          === This is because of the following config errors: ===";
  public static final String USED_GROUPS_PATH = GENERATED_COMBINE_ITEMS_DIR + File.separator + "usedGroups.json";
  @Getter
  @Setter
  private static PrepareItemsMojo instance;
  private final ApiRequester apiRequester;
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
   * Name a list of lists that should be used and how often.
   */
  @Parameter(property = "poolGroups")
  @Getter
  List<PoolGroup> poolGroups;
  /**
   * Name of all groups of items in combine_items.json explicitly not to use
   */
  @Parameter(property = "excludedGroups")
  List<String> excludedGroups;
  /**
   * Size of to use different groups
   */
  @Parameter(property = "poolSize", defaultValue = "0")
  int poolSize;
  /**
   * This is another way to define poolGroups. Will override 'poolGroups' Pattern GroupPattern is
   * mandatory. Amount == (0 || null) means all, Strategy == null -> default strategy
   * <PoolGroup><GroupPattern>|<GroupPattern>,<Amount>,<Strategy></PoolGroup>;<PoolGroup><GroupPattern>|<GroupPattern>,<Amount>,<Strategy></PoolGroup>
   */
  @Parameter(property = "poolGroupString")
  String poolGroupString;
  /**
   * Default matching strategy. If no strategy is named in poolGroup this will be applied
   */
  @Parameter(name = "defaultMatchStrategy", defaultValue = "WILDCARD")
  @Getter
  GroupMatchStrategyType defaultMatchStrategy;
  /**
   * Parameter to decide if prepare-execution should be run
   */
  @Parameter(name = "skipPrep", defaultValue = "false")
  boolean skipPrep;

  private ItemsCreator itemsCreator;
  private List<CombineItem> items;
  private Pooler pooler;
  private PrepareItemsConfig config;

  public static Log getPluginLog() {
    return instance.getLog();
  }

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    if (this.isSkip() || skipPrep) {
      getLog().warn("Preparation of items file got skipped due configuration");
      return;
    }
    setInstance(this);
    checkExpressionSetCorrectly();
    getLog().info("Going to preprocess " + getCombineItemsFile());
    if (StringUtils.isNotBlank(poolGroupString)) {
      getLog().info("String is used to create poolGroups. Overrides other configuration!");
      this.poolGroups = new PoolGroupParser().transformStringToGroups(poolGroupString);
    }
    setDefaultForEmptyStrategyInPoolGroups();
    config = getCreateItemsConfig();
    itemsCreator = new ItemsCreator(config);
    pooler = new Pooler(config);
    items = pooler.pool();
    writeUsedGroupsToFile(items);
    apiRequester.setupProxy(getProxyHost(), getProxyPort());
    apiRequester
        .setupTls(getTruststore(), getTruststorePw(), getClientCertStore(), getClientCertStorePw());
    apiRequester.setAllowedResponses(config.getAcceptedResponseFamilies(), config.getAllowedResponseCodes());
    run();
  }

  private void setDefaultForEmptyStrategyInPoolGroups() {
    poolGroups = poolGroups.stream().map(p -> {
      if (p.getStrategy() == null) {
        p.setStrategy(defaultMatchStrategy);
      }
      return p;
    }).collect(toList());
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
    boolean requestsOk = apiErrors.isEmpty() || !isBreakOnFailedRequest();
    boolean contextOk = itemsCreator.getContextErrors().isEmpty() || !isBreakOnContextError();
    writeErrors(getClass().getSimpleName(), apiErrors, FAILED_REQ_WARN_MESSAGE, false);
    writeErrors(getClass().getSimpleName(), itemsCreator.getContextErrors(),
        CONFIG_FAIL_WARN_MESSAGE, apiErrors.isEmpty());
    if (requestsOk && contextOk) {
      getLog().info(
          format("Successfully processed %s items, writing to file %s",
              processedItems.size(), getCombineItemsFile()));
      writeItemsToFile(processedItems);
    }
    if (!requestsOk) {
      throw new MojoExecutionException(
          "Error occurred for following API`s ->\n" + join("\n", apiErrors));
    }
    if (!contextOk) {
      throw new MojoExecutionException(
          "Different tags or properties where found ->\n" + join("\n",
              itemsCreator.getContextErrors()));
    }
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
      apiErrors.add(getItemAsString(item) + " -> not reachable");
      getLog().error("Could not connect to api: " + getItemAsString(item) + "\n" + ex.getMessage());
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
        GENERATED_COMBINE_ITEMS_DIR + File.separator + new File(getCombineItemsFile()).getName();
    getLog().info("Created new combine item file -> " + fileName);
    FileUtils.writeStringToFile(
        new File(fileName),
        new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(items), UTF_8);
  }

  @SneakyThrows
  private void writeUsedGroupsToFile(List<CombineItem> finalListOfItems) {
    Map<String, List<String>> usedGroups =
        finalListOfItems.stream()
            .map(CombineItem::getGroups)
            .flatMap(Collection::stream)
            .distinct()
            .filter(pooler::matchAny)
            .collect(Collectors.toMap(
                String::toString,
                e -> finalListOfItems.stream()
                    .filter(i -> i.getGroups().contains(e))
                    .map(CombineItem::produceValueUrl)
                    .collect(toList())));
    List<String> usedItems = finalListOfItems.stream().map(CombineItem::produceValueUrl).collect(toList());
    JSONObject result = new JSONObject();
    result.put("usedGroups", usedGroups);
    result.put("excludedGroups", excludedGroups);
    result.put("poolGroups", poolGroups);
    result.put("usedItems", usedItems);
    File file = new File(USED_GROUPS_PATH);
    FileUtils.writeStringToFile(file, result.toString(), UTF_8);
    System.setProperty("cutest.plugin.groups.usedGroups", mapGroupsToString(usedGroups));
    System.setProperty("cutest.plugin.groups.excluded", join(",", excludedGroups));
    System.setProperty("cutest.plugin.groups.poolGroupString", poolGroupsToString());
    System.setProperty("cutest.plugin.groups.usedItems", join(",", usedItems));
    getLog().info("Created new used group file -> " + file.getAbsolutePath());
  }

  private String poolGroupsToString() {
    return join(";", poolGroups.stream().map(PoolGroup::toPoolGroupString).collect(toList()));
  }

  private String mapGroupsToString(Map<String, List<String>> map) {
    StringBuilder sb = new StringBuilder();
    map.forEach((k, v) -> sb.append(" [" + k + "]: " + join(" | ", v)));
    return sb.toString();
  }

  private PrepareItemsConfig getCreateItemsConfig() {
    return PrepareItemsConfig.builder()
        .combineItemsFile(getCombineItemsFile())
        .infoResourceLocation(infoResourceLocation)
        .tagExpressions(tagExpressions)
        .propertyExpressions(propertyExpressions)
        .poolGroups(poolGroups)
        .excludedGroups(excludedGroups)
        .poolSize(poolSize)
        .defaultMatchStrategy(defaultMatchStrategy)
        .acceptedResponseFamilies(getAcceptedResponseFamilies())
        .allowedResponseCodes(getAllowedResponseCodes())
        .build();
  }
}

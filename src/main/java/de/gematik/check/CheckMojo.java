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

package de.gematik.check;

import static de.gematik.utils.Utils.getItemAsString;
import static de.gematik.utils.Utils.getItemsToCombine;
import static de.gematik.utils.Utils.writeErrors;
import static java.lang.Boolean.FALSE;
import static java.lang.String.format;
import static java.util.Objects.nonNull;

import de.gematik.BaseMojo;
import de.gematik.combine.model.CombineItem;
import de.gematik.utils.request.ApiRequester;
import io.cucumber.core.internal.com.fasterxml.jackson.core.JsonProcessingException;
import io.cucumber.core.internal.com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlContext;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.JexlException;
import org.apache.commons.jexl3.JexlExpression;
import org.apache.commons.jexl3.MapContext;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Plugin checks api with jexl expression
 */
@Setter
@Getter
@Mojo(name = "check", defaultPhase = LifecyclePhase.GENERATE_TEST_SOURCES)
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class CheckMojo extends BaseMojo {

  @Setter
  @Getter
  private static CheckMojo instance;
  private final ApiRequester apiRequester;
  private final List<String> errors = new ArrayList<>();

  /**
   * Path that will be used for checking
   */
  @Parameter(property = "checkPath")
  String checkPath;
  /**
   * List of Expression with id that could be referenced in combine_item
   */
  @Parameter(property = "checkExpressions")
  List<CheckExpression> checkExpressions = new ArrayList<>();
  /**
   * Jexl statement that will be applied on the result of the checkPath result if no other
   * expression is mentioned
   */
  @Parameter(property = "defaultCheckExpressions")
  String defaultCheckExpressions;

  public static Log getPluginLog() {
    return instance.getLog();
  }

  public static final JexlEngine JEXL_ENGINE = new JexlBuilder()
      .strict(true)
      .silent(false)
      .safe(false)
      .create();

  @Override
  public void execute() {
    setInstance(this);
    this.apiRequester
        .setupTls(getTruststore(), getTruststorePw(), getClientCertStore(), getClientCertStorePw());
    run();
  }

  @SneakyThrows
  private void run() {
    List<CombineItem> items = getItemsToCombine(new File(getCombineItemsFile()), this, true);
    items.forEach(this::check);
    writeErrors(getClass().getSimpleName(), errors);
    if (!errors.isEmpty()) {
      throw new MojoExecutionException(
          "Something went wrong during api check! At least one of your api could not pass the check. See error log at "
              + GENERATED_COMBINE_ITEMS_DIR);
    }
  }

  private void check(CombineItem item) {
    JexlExpression expression;
    try {
      String expressionString = getExpression(item);
      getLog().info(format("Checking %s with expression \"%s\"", getItemAsString(item),
          expressionString.replace("\n","")));
      expression = JEXL_ENGINE.createExpression(expressionString);
    } catch (MojoExecutionException | JexlException e) {
      getLog().error(e.getMessage());
      errors.add(e.getMessage());
      return;
    }

    try {
      String url = item.getUrl() == null ? item.getValue() : item.getUrl();
      Map<?, ?> jsonContext = getJsonContextFromApi(url + "/" + checkPath);
      final JexlContext context = new MapContext();
      context.set("$", jsonContext);
      context.set("ITEM", item);
      if (FALSE.equals(expression.evaluate(context))) {
        errors.add(format("Check fails for %s and expression \"%s\"", getItemAsString(item),
            expression));
      }
    } catch (MojoExecutionException e) {
      getLog().error(e.getMessage());
      errors.add(e.getMessage());
    } catch (JsonProcessingException e) {
      String errorMsg = format("Requested check endpoint for %s but was not well formatted",
          getItemAsString(item));
      getLog().error(errorMsg);
      errors.add(errorMsg);
    }
  }

  private String getExpression(CombineItem combineItem) throws MojoExecutionException {
    if (nonNull(combineItem.getCheckExpression())) {
      return combineItem.getCheckExpression();
    }
    return checkExpressions.stream()
        .filter(e -> e.getId().equals(combineItem.getCheckExpressionId()))
        .map(CheckExpression::getExpression)
        .findFirst().orElse(getDefaultCheckExpressions(combineItem));
  }

  private String getDefaultCheckExpressions(CombineItem item) throws MojoExecutionException {
    if (nonNull(defaultCheckExpressions)) {
      return defaultCheckExpressions;
    }
    if (checkExpressions.isEmpty()) {
      String errorMsg = "For item " + getItemAsString(item) + " no checkExpression could be found!";
      getLog().error(errorMsg);
      throw new MojoExecutionException(errorMsg);
    }
    return checkExpressions.get(0).getExpression();
  }

  private Map<?,?> getJsonContextFromApi(String url)
      throws MojoExecutionException, JsonProcessingException {
    return new ObjectMapper().readValue(apiRequester.getApiResponse(url), Map.class);
  }

}

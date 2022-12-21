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


import static de.gematik.prepare.PrepareItemsMojo.getPluginLog;
import static java.util.Objects.nonNull;

import de.gematik.combine.model.CombineItem;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import lombok.Getter;
import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlContext;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.JexlException;
import org.apache.commons.jexl3.MapContext;

public class ItemsCreator {

  private static final String ERROR_START = "Error for api: ";
  private final PrepareItemsConfig config;
  private JexlContext context;

  @Getter
  private final List<String> configErrors = new ArrayList<>();

  public static final JexlEngine JEXL_ENGINE = new JexlBuilder()
      .strict(true)
      .silent(false)
      .safe(false)
      .create();

  public ItemsCreator(PrepareItemsConfig config) {
    this.config = config;
  }

  public Void evaluateExpressions(CombineItem item, Map<?, ?> jsonContext) {
    context = new MapContext();
    context.set("$", jsonContext);
    config.getTagExpressions().forEach(e -> checkTagExpression(item, e));
    config.getPropertyExpressions().forEach(e -> checkPropertyExpression(item, e));
    return null;
  }

  private void checkTagExpression(CombineItem item, TagExpression tagExpression) {
    getPluginLog().debug("Evaluating tag expression -> " + tagExpression.getExpression() + " - "
        + tagExpression.getTag());
    var newTags = new HashSet<>(item.getTags());

    try {
      Boolean result = (Boolean) JEXL_ENGINE.createExpression(tagExpression.getExpression())
          .evaluate(context);
      if (result != null && result) {
        newTags.add(tagExpression.getTag());
      } else {
        if (item.getTags().contains(tagExpression.getTag())) {
          configErrors.add(
              ERROR_START + item.getValue() + " -> for tag " + tagExpression.getTag()
                  + " extension "
                  + tagExpression.getExpression()
                  + " should return true");
          newTags.remove(tagExpression.getTag());
        }
      }
    } catch (JexlException ex) {
      getPluginLog().warn(ex.getMessage(), ex);
      throw ex;
    }
    item.setTags(newTags);
  }

  private void checkPropertyExpression(CombineItem item, PropertyExpression propertyExpression) {
    Entry<String, String> entry = evaluatePropertyExpression(propertyExpression,
        item.getProperties(), item);
    var propertiesCopy = new HashMap<>(item.getProperties());
    if (entry != null) {
      propertiesCopy.put(entry.getKey(), entry.getValue());
      item.setProperties(propertiesCopy);
    } else {
      item.getProperties().remove(propertyExpression.getProperty());
    }
  }

  private Entry<String, String> evaluatePropertyExpression(PropertyExpression propertyExpression,
      Map<String, String> existingProperties, CombineItem item) {
    getPluginLog().debug("evaluating " + propertyExpression.getExpression());
    String value;
    try {
      value = (String) JEXL_ENGINE.createExpression(propertyExpression.getExpression())
          .evaluate(context);
    } catch (JexlException ex) {
      getPluginLog().warn(ex.getMessage());
      return null;
    }
    if (value == null) {
      configErrors.add(
          ERROR_START + item.getValue() + (nonNull(item.getUrl()) ? " url: " + item.getUrl()
              : "") + " -> at property "
              + propertyExpression.getProperty() + ": Could not find any value at -> "
              + propertyExpression.getExpression());
      return null;
    }
    if (existingProperties.containsKey(propertyExpression.getProperty())) {
      String existingProperty = existingProperties.get(propertyExpression.getProperty());
      if (!value.equals(existingProperty)) {
        configErrors.add(
            ERROR_START + item.getValue() + (nonNull(item.getUrl()) ? " url: " + item.getUrl()
                : "") + " -> at property "
                + propertyExpression.getProperty() + ": Found value -> \"" + value
                + "\" differ from -> \"" + existingProperty + "\" for expression -> "
                + propertyExpression.getExpression());
      }
    }
    Entry<String, String> keyValue = Map.entry(propertyExpression.getProperty(), value);
    getPluginLog().debug("proceeded property -> " + keyValue);
    return keyValue;

  }

}



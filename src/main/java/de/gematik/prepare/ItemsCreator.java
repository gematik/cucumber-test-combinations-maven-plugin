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

import de.gematik.combine.model.CombineItem;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlContext;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.JexlException;
import org.apache.commons.jexl3.MapContext;

public class ItemsCreator {

  private final PrepareItemsConfig config;
  private JexlContext context;

  public static final JexlEngine JEXL_ENGINE = new JexlBuilder()
      .strict(true)
      .silent(false)
      .safe(false)
      .create();

  public ItemsCreator(PrepareItemsConfig config) {
    this.config = config;
  }

  public void evaluateExpressions(CombineItem item, Map<?, ?> jsonContext) {
    context = new MapContext();
    context.set("$", jsonContext);
    config.getTagExpressions().forEach(e -> checkTagExpression(item, e));
    config.getPropertyExpressions().forEach(e -> checkPropertyExpression(item, e));
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
        newTags.remove(tagExpression.getTag());
      }
    } catch (JexlException ex) {
      getPluginLog().warn(ex.getMessage());
    }
    item.setTags(newTags);
  }

  private void checkPropertyExpression(CombineItem item, PropertyExpression propertyExpression) {
    Entry<String, String> entry = evaluatePropertyExpression(propertyExpression);
    if (entry != null) {
      var propertiesCopy = new HashMap<>(item.getProperties());
      propertiesCopy.put(entry.getKey(), entry.getValue());
      item.setProperties(propertiesCopy);
    }
  }

  private Entry<String, String> evaluatePropertyExpression(PropertyExpression propertyExpression) {
    getPluginLog().debug("evaluating " + propertyExpression.getExpression());
    try {
      Entry<String, String> keyValue = Map.entry(propertyExpression.getProperty(),
          (String) JEXL_ENGINE.createExpression(propertyExpression.getExpression())
              .evaluate(context));
      getPluginLog().debug("created property " + keyValue);
      return keyValue;
    } catch (JexlException ex) {
      getPluginLog().warn(ex.getMessage());
      return null;
    }
  }

}



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

import static de.gematik.utils.MockPluginLog.withMockedPluginLog;
import static java.util.Collections.emptySet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import de.gematik.combine.model.CombineItem;
import io.cucumber.core.internal.com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ItemsCreatorTest extends AbstractPrepareTest {

  private final ObjectMapper objectMapper = new ObjectMapper();

  private ItemsCreator creator;

  @Nested
  @TestInstance(PER_CLASS)
  class Tags {

    public Stream<Arguments> tagTests() {
      return Stream.of(
          arguments("set tag", "{\"key\": true}", "$.key", true),
          arguments("unset tag", "{\"key\": false}", "$.key", false),
          arguments("check invalid path", "{\"someKey\": false}", "$.nonExistent", false),
          arguments("set tag from nested path", "{\"nested\": {\"key\": true}}", "$.nested.key",
              true),
          arguments("set tag from compare", "{\"key\": \"value\"}", "$.key=='value'", true),
          arguments("unset tag from compare", "{\"key\": \"value\"}", "$.key=='other'", false)
      );
    }

    @SneakyThrows
    @MethodSource
    @ParameterizedTest(name = "{0}")
    void tagTests(String description, String json, String expression, boolean isTagSet) {
      // arrange
      String tagName = "newTag";
      creator = new ItemsCreator(PrepareItemsConfig.builder()
          .tagExpressions(List.of(new TagExpression(tagName, expression)))
          .propertyExpressions(List.of())
          .build());

      Map<?, ?> context = objectMapper.readValue(json, Map.class);

      CombineItem combineItem = CombineItem.builder()
          .value("someItem")
          .build();

      // act
      creator.evaluateExpressions(combineItem, context);

      // assert
      assertThat(combineItem.getTags()).isEqualTo(isTagSet ? Set.of(tagName) : emptySet());
    }

    public Stream<Arguments> alterTagTests() {
      return Stream.of(
          arguments("add new tag", Set.of("initialTag"), "{\"key\": true}",
              new TagExpression("otherTag", "$.key"), Set.of("initialTag", "otherTag")),
          arguments("set existing tag again", Set.of("initialTag"), "{\"key\": true}",
              new TagExpression("initialTag", "$.key"), Set.of("initialTag")),
          arguments("remove existing tag", Set.of("initialTag"), "{\"key\": false}",
              new TagExpression("initialTag", "$.key"), Set.of()),
          arguments("expression not found and remove", Set.of("initialTag"),
              "{\"otherKey\": false}",
              new TagExpression("initialTag", "$.key"), Set.of())
      );
    }

    @SneakyThrows
    @MethodSource
    @ParameterizedTest(name = "{0}")
    void alterTagTests(String description, Set<String> initialTags, String json,
        TagExpression expression, Set<String> expectedTags) {
      // arrange
      creator = new ItemsCreator(PrepareItemsConfig.builder()
          .tagExpressions(List.of(expression))
          .propertyExpressions(List.of())
          .build());

      Map<?, ?> context = objectMapper.readValue(json, Map.class);

      CombineItem combineItem = CombineItem.builder()
          .value("someItem")
          .tags(initialTags)
          .build();

      // act
      creator.evaluateExpressions(combineItem, context);

      // assert
      assertThat(combineItem.getTags()).isEqualTo(expectedTags);
    }

    public Stream<Arguments> addErrorTests() {
      return Stream.of(
          arguments("Tag not found", new TagExpression("myTag", "$.target"),
              "{\"differentTarget\": true}", Set.of("myTag")),
          arguments("Tag found but false", new TagExpression("myTag", "$.target"),
              "{\"target\": false}", Set.of("myTag")),
          arguments("Nested tag not found", new TagExpression("myTag", "$.app.target"),
              "{\"app\": {\"differentTarget\": false}}", Set.of("myTag")),
          arguments("Nested tag found but false", new TagExpression("myTag", "$.app.target"),
              "{\"app\": {\"target\": false}}", Set.of("myTag"))
      );
    }


    @SneakyThrows
    @MethodSource
    @ParameterizedTest(name = "{0}")
    void addErrorTests(String description, TagExpression expression, String json,
        Set<String> initialTags) {
      // arrange
      creator = new ItemsCreator(PrepareItemsConfig.builder()
          .tagExpressions(List.of(expression))
          .propertyExpressions(List.of())
          .build());
      Map<?, ?> context = objectMapper.readValue(json, Map.class);

      CombineItem combineItem = CombineItem.builder()
          .value("http://myApi.com:8080")
          .tags(initialTags)
          .build();

      // act
      creator.evaluateExpressions(combineItem, context);

      // assert
      assertThat(creator.getContextErrors()).as("Test \"" + description + "\" failed!").hasSize(1);
    }

  }

  @Nested
  @TestInstance(PER_CLASS)
  class Properties {

    public Stream<Arguments> propertyTests() {
      return Stream.of(
          arguments("add property", Map.of(), "{\"key\": \"newValue\"}",
              new PropertyExpression("prop", "$.key"), Map.of("prop", "newValue")),
          arguments("override property", Map.of("prop", "oldValue"), "{\"key\": \"newValue\"}",
              new PropertyExpression("prop", "$.key"), Map.of("prop", "newValue")),
          arguments("add additional property", Map.of("existing", "value"),
              "{\"key\": \"newValue\"}",
              new PropertyExpression("prop", "$.key"),
              Map.of("existing", "value", "prop", "newValue")),
          arguments("add property from deep path", Map.of(),
              "{\"nested\":{\"prop\": \"newValue\"}}",
              new PropertyExpression("prop", "$.nested.prop"), Map.of("prop", "newValue")),
          arguments("expression does not exist but was set", Map.of(),
              "{\"prop\": \"newValue\"}",
              new PropertyExpression("prop", "$.notExistingProp"), Map.of()),
          arguments("expression does not exist and not set", Map.of(), "{\"prop\": \"newValue\"}",
              new PropertyExpression("prop", "$.notExistingProp"), Map.of()),
          arguments("add property with expression", Map.of(), "{\"key\": true}}",
              new PropertyExpression("prop", "$.key==true ? 'yes' : 'no'"), Map.of("prop", "yes"))
      );
    }

    @SneakyThrows
    @MethodSource
    @ParameterizedTest(name = "{0}")
    void propertyTests(String description, Map<String, String> initialProperties, String json,
        PropertyExpression expression, Map<String, String> expectedProperties) {
      // arrange
      creator = new ItemsCreator(PrepareItemsConfig.builder()
          .tagExpressions(List.of())
          .propertyExpressions(List.of(expression))
          .build());

      Map<?, ?> context = objectMapper.readValue(json, Map.class);

      CombineItem combineItem = CombineItem.builder()
          .value("http://myApi.com:8080")
          .properties(initialProperties)
          .build();

      // act
      withMockedPluginLog(() -> creator.evaluateExpressions(combineItem, context));

      // assert
      assertThat(combineItem.getProperties().entrySet()).isEqualTo(expectedProperties.entrySet());
    }

    public Stream<Arguments> propertyErrorTests() {
      return Stream.of(
          arguments("property not found", Map.of(), "{\"otherTarget\": \"newValue\"}",
              new PropertyExpression("prop", "$.target")),
          arguments("different property", Map.of("prop", "jsonValue"),
              "{\"target\": \"propValue\"}", new PropertyExpression("prop", "$.target")),
          arguments("nested property not found", Map.of(),
              "{\"nested\":{\"differentTarget\": \"newValue\"}}",
              new PropertyExpression("prop", "$.nested.target")),
          arguments("nested different property", Map.of("prop", "jsonValue"),
              "{\"nested\":{\"target\": \"newValue\"}}",
              new PropertyExpression("prop", "$.nested.target"))
      );
    }

    @SneakyThrows
    @MethodSource
    @ParameterizedTest(name = "{0}")
    void propertyErrorTests(String description, Map<String, String> initialProperties, String json,
        PropertyExpression expression) {
      // arrange
      creator = new ItemsCreator(PrepareItemsConfig.builder()
          .tagExpressions(List.of())
          .propertyExpressions(List.of(expression))
          .build());

      Map<?, ?> context = objectMapper.readValue(json, Map.class);

      CombineItem combineItem = CombineItem.builder()
          .value("http://myApi.com:8080")
          .properties(initialProperties)
          .build();

      // act
      creator.evaluateExpressions(combineItem, context);

      // assert
      assertThat(creator.getContextErrors()).as("Test \"" + description + "\" failed!").hasSize(1);
    }
  }
}
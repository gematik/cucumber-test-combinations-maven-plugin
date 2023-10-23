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

package de.gematik.combine.filter.table.row;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import de.gematik.combine.model.CombineItem;
import de.gematik.combine.model.TableCell;
import java.util.List;
import java.util.stream.Stream;
import lombok.SneakyThrows;
import org.apache.maven.plugin.MojoExecutionException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class JexlRowFilterTest {

  List<TableCell> tableCells = List.of(
      TableCell.builder()
          .header("A")
          .combineItem(CombineItem.builder()
              .tag("orgAdmin")
              .tag("new")
              .property("homeserver", "X")
              .property("priority", "1")
              .value("orgAdminX")
              .build())
          .build(),
      TableCell.builder()
          .header("B")
          .combineItem(CombineItem.builder()
              .tag("client")
              .tag("new")
              .property("homeserver", "X")
              .property("priority", "2")
              .value("clientY")
              .build())
          .build()
  );

  static Stream<Arguments> expressionTests() {
    return Stream.of(
        // tags
        arguments(true, "A.hasTag(\"orgAdmin\")"),
        arguments(false, "A.hasTag(\"client\")"),
        arguments(false, "B.hasTag(\"orgAdmin\")"),
        arguments(true, "B.hasTag(\"client\")"),
        arguments(true, "allTags.size()==3"),
        arguments(true, "allTags.containsKey(\"new\")"),
        arguments(true, "allTags[\"new\"]==2"),
        // properties
        arguments(true, "A.hasProperty(\"homeserver\")"),
        arguments(false, "A.hasProperty(\"foo\")"),
        arguments(true, "A.properties[\"homeserver\"].equals(\"X\")"),
        arguments(true, "A.properties[\"homeserver\"].equals(B.properties[\"homeserver\"])"),
        arguments(false, "A.properties[\"homeserver\"].equals(\"foo\")"),
        arguments(true, "allProperties[\"homeserver\"].contains(\"X\")")
    );
  }

  @ParameterizedTest
  @MethodSource("expressionTests")
  @SneakyThrows
  void evaluateExpressions(boolean shouldMatch, String expression) {
    // arrange
    JexlRowFilter filter = new JexlRowFilter(expression);
    // act
    boolean actualMatch = filter.test(tableCells);
    // assert
    assertEquals(shouldMatch, actualMatch);
  }

  @Test
  @SneakyThrows
  void shouldReportWrongExpression() {
    // arrange
    JexlRowFilter filter = new JexlRowFilter("expression");
    // act
    assertThatThrownBy(() -> filter.test(tableCells))
        // assert
        .isInstanceOf(MojoExecutionException.class)
        .hasMessageContainingAll("Could not evaluate expression 'expression'",
            "variable 'expression' is undefined");
  }

}

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

package de.gematik.combine.execution;

import static de.gematik.combine.CombineMojo.getPluginLog;
import static java.lang.String.format;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;

import de.gematik.combine.CombineConfiguration;
import de.gematik.combine.filter.table.TableFilter;
import de.gematik.combine.model.CombineItem;
import de.gematik.combine.model.TableCell;
import de.gematik.combine.tags.ConfiguredFilters;
import de.gematik.combine.tags.ParsedTags;
import de.gematik.combine.tags.TagParser;
import io.cucumber.messages.types.Examples;
import io.cucumber.messages.types.Location;
import io.cucumber.messages.types.TableRow;
import io.cucumber.messages.types.Tag;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@RequiredArgsConstructor(onConstructor_ = @Inject)
public class ExamplesProcessor {

  private static final Location LOCATION = new Location(0L, 0L);

  private final TagParser tagParser;

  private final TableGenerator tableGenerator;

  public void process(Examples gherkinExamples, CombineConfiguration defaultConfiguration,
      List<CombineItem> combineItems) {
    if (!defaultConfiguration.getDefaultExamplesTags().isEmpty()) {
      addDefaultTags(gherkinExamples, defaultConfiguration.getDefaultExamplesTags());
    }

    List<String> headers = extractHeaders(gherkinExamples);
    List<String> tagStrings = extractTagStrings(gherkinExamples);

    ParsedTags parsedTags = tagParser.parseTags(tagStrings, headers);
    ConfiguredFilters filters = parsedTags.configureFilters(
        defaultConfiguration.getFilterConfiguration());

    List<List<TableCell>> filteredTable = generateTable(combineItems, filters);

    getPluginLog().debug("converting table to gherkin format");
    List<TableRow> gherkinTable = filteredTable.stream()
        .map(this::toTableRow)
        .collect(toList());

    setTableBody(gherkinExamples, gherkinTable);
    setPluginTagPrefix(gherkinExamples, defaultConfiguration.getPluginTagCategory());
  }

  private void addDefaultTags(Examples gherkinExamples, List<String> defaultTags) {
    ArrayList<Tag> tags = new ArrayList<>(gherkinExamples.getTags());
    tags.addAll(defaultTags.stream()
        .map(tagStr -> new Tag(LOCATION, tagStr, tagStr))
        .collect(toList()));
    setTags(gherkinExamples, tags);
  }

  private List<List<TableCell>> generateTable(List<CombineItem> combineItems,
      ConfiguredFilters filters) {

    List<List<TableCell>> baseTable = generateBaseTable(combineItems, filters);

    return filterTable(baseTable, filters);
  }

  private List<List<TableCell>> filterTable(List<List<TableCell>> baseTable,
      ConfiguredFilters filters) {
    List<TableFilter> tableFilters = new ArrayList<>(filters.getTableFilters());
    tableFilters.addAll(filters.getTableRowFilters());

    getPluginLog().debug(format("applying %d filters: %s", tableFilters.size(), tableFilters));

    return filters.combineAllFilters().apply(baseTable);
  }

  private List<List<TableCell>> generateBaseTable(List<CombineItem> combineItems,
      ConfiguredFilters filters) {
    return tableGenerator.generateTable(combineItems, filters);
  }

  @SneakyThrows
  @SuppressWarnings("java:S3011")
  public void setTableBody(Examples examples, List<TableRow> table) {
    Field field = Examples.class.getDeclaredField("tableBody");
    field.setAccessible(true);
    field.set(examples, table);
  }

  @SneakyThrows
  @SuppressWarnings("java:S3011")
  public void setTags(Examples examples, List<Tag> tags) {
    Field field = Examples.class.getDeclaredField("tags");
    field.setAccessible(true);
    field.set(examples, tags);
  }

  @SneakyThrows
  @SuppressWarnings("java:S3011")
  private void setPluginTagPrefix(Examples examples, String pluginTagCategory) {
    List<Tag> changedTags = examples.getTags().stream()
        .map(tag -> addPluginPrefixToTag(tag, pluginTagCategory))
        .collect(toList());

    Field field = Examples.class.getDeclaredField("tags");
    field.setAccessible(true);
    field.set(examples, changedTags);
  }

  private static Tag addPluginPrefixToTag(Tag tag, String pluginTagCategory) {
    String newTagStr = String.format("@%s:%s", pluginTagCategory, tag.getName().substring(1));
    return new Tag(LOCATION, newTagStr, tag.getId());
  }

  private TableRow toTableRow(List<TableCell> row) {
    final List<io.cucumber.messages.types.TableCell> cells = row.stream()
        .map(value -> new io.cucumber.messages.types.TableCell(LOCATION, value.getValue()))
        .collect(toList());

    return new TableRow(LOCATION, cells, randomUUID().toString());
  }

  private List<String> extractHeaders(Examples examples) {
    return examples.getTableHeader().orElseThrow()
        .getCells().stream()
        .map(io.cucumber.messages.types.TableCell::getValue)
        .collect(toList());
  }

  private List<String> extractTagStrings(Examples examples) {
    return examples.getTags().stream()
        .map(Tag::getName)
        .collect(toList());
  }
}

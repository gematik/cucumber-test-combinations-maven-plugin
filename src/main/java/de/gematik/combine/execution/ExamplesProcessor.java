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

package de.gematik.combine.execution;

import static de.gematik.combine.CombineMojo.ErrorType.WARNING;
import static de.gematik.combine.CombineMojo.appendError;
import static de.gematik.combine.CombineMojo.getPluginLog;
import static de.gematik.combine.FilterTagMapper.filterToTag;
import static de.gematik.combine.FilterTagMapper.getTagName;
import static java.lang.String.format;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;

import de.gematik.combine.CombineConfiguration;
import de.gematik.combine.filter.project.ProjectCellFilter;
import de.gematik.combine.filter.project.ProjectRowFilter;
import de.gematik.combine.filter.table.TableFilter;
import de.gematik.combine.filter.table.cell.VersionFilter;
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
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@RequiredArgsConstructor(onConstructor_ = @Inject)
public class ExamplesProcessor {

  private static final Location LOCATION = new Location(0L, 0L);
  private final TagParser tagParser;
  private final TableGenerator tableGenerator;

  public void process(
      Examples gherkinExample,
      CombineConfiguration config,
      List<CombineItem> combineItems,
      String scenarioName) {

    if (!config.getDefaultExamplesTags().isEmpty()) {
      addDefaultTags(gherkinExample, config.getDefaultExamplesTags());
    }

    List<String> headers = extractHeaders(gherkinExample);
    List<String> tagStrings = extractTagStrings(gherkinExample);

    ParsedTags parsedTags = tagParser.parseTags(tagStrings, headers);
    generateTable(gherkinExample, config, combineItems, parsedTags, true);
    boolean tableToSmall = gherkinExample.getTableBody().size() < config.getMinTableSize();
    if (tableToSmall && !config.isSoftFilterToHardFilter() && parsedTags.containSoftFilter()) {
      appendError(
          format(
              "For scenario \"%s\" no table could be generated. Going to retry without SoftFilter",
              scenarioName),
          WARNING);
      generateTable(gherkinExample, config, combineItems, parsedTags, false);
    }
    addPluginTagPrefixes(gherkinExample, config);
  }

  private void generateTable(
      Examples gherkinExample,
      CombineConfiguration config,
      List<CombineItem> combineItems,
      ParsedTags parsedTags,
      boolean softFilterShouldApply) {

    ConfiguredFilters filters = parsedTags.configureFilters(config, softFilterShouldApply);

    List<List<TableCell>> filteredTable = generateTable(combineItems, filters);

    getPluginLog().debug("converting table to gherkin format");
    List<TableRow> gherkinTable =
        filteredTable.stream().map(ExamplesProcessor::toTableRow).collect(toList());

    setTableBody(gherkinExample, gherkinTable);
    addAppliedProjectFilters(gherkinExample, filters);
  }

  private List<List<TableCell>> generateTable(
      List<CombineItem> combineItems, ConfiguredFilters filters) {

    List<List<TableCell>> baseTable = generateBaseTable(combineItems, filters);

    return filterTable(baseTable, filters);
  }

  private List<List<TableCell>> filterTable(
      List<List<TableCell>> baseTable, ConfiguredFilters filters) {
    List<TableFilter> tableFilters = new ArrayList<>(filters.getTableFilters());
    tableFilters.addAll(filters.getTableRowFilters());

    getPluginLog().debug(format("applying %d filters: %s", tableFilters.size(), tableFilters));

    return filters.combineAllFilters().apply(baseTable);
  }

  private List<List<TableCell>> generateBaseTable(
      List<CombineItem> combineItems, ConfiguredFilters filters) {
    return tableGenerator.generateTable(combineItems, filters);
  }

  @SneakyThrows
  @SuppressWarnings("java:S3011")
  private static void setTableBody(Examples examples, List<TableRow> table) {
    Field field = Examples.class.getDeclaredField("tableBody");
    field.setAccessible(true);
    field.set(examples, table);
  }

  private static void addDefaultTags(Examples examples, List<String> defaultTags) {
    ArrayList<Tag> tags = new ArrayList<>(examples.getTags());
    tags.addAll(
        defaultTags.stream().map(tagStr -> new Tag(LOCATION, tagStr, tagStr)).collect(toList()));
    setTags(examples, tags);
  }

  private static void addAppliedProjectFilters(Examples examples, ConfiguredFilters filters) {
    ArrayList<Tag> tags = new ArrayList<>(examples.getTags());
    tags.addAll(getAppliedProjectCellFilters(filters));
    tags.addAll(getAppliedProjectTableRowFilters(filters));
    setTags(examples, tags);
  }

  @SneakyThrows
  @SuppressWarnings("java:S3011")
  private static void addPluginTagPrefixes(Examples examples, CombineConfiguration config) {
    List<Tag> changedTags =
        examples.getTags().stream()
            .map(
                tag ->
                    addPrefixToTag(
                        tag,
                        tag.getName().substring(1).startsWith(getTagName(VersionFilter.class), 0)
                            ? config.getVersionFilterTagCategory()
                            : config.getPluginTagCategory()))
            .collect(toList());
    setTags(examples, changedTags);
  }

  @SneakyThrows
  @SuppressWarnings("java:S3011")
  private static void setTags(Examples examples, List<Tag> tags) {
    Field field = Examples.class.getDeclaredField("tags");
    field.setAccessible(true);
    field.set(examples, tags);
  }

  private static TableRow toTableRow(List<TableCell> row) {
    final List<io.cucumber.messages.types.TableCell> cells =
        row.stream()
            .map(value -> new io.cucumber.messages.types.TableCell(LOCATION, value.getValue()))
            .collect(toList());

    return new TableRow(LOCATION, cells, randomUUID().toString());
  }

  private static List<String> extractHeaders(Examples examples) {
    return examples.getTableHeader().orElseThrow().getCells().stream()
        .map(io.cucumber.messages.types.TableCell::getValue)
        .collect(toList());
  }

  private static List<String> extractTagStrings(Examples examples) {
    return examples.getTags().stream().map(Tag::getName).collect(toList());
  }

  private static List<Tag> getAppliedProjectTableRowFilters(ConfiguredFilters filters) {
    return filters.getTableRowFilters().stream()
        .filter(ProjectRowFilter.class::isInstance)
        .map(filter -> getProjectRowFilterTag((ProjectRowFilter) filter))
        .collect(toList());
  }

  private static List<Tag> getAppliedProjectCellFilters(ConfiguredFilters filters) {
    return filters.getCellFilters().entrySet().stream()
        .map(
            column ->
                new SimpleEntry<>(
                    column.getKey(),
                    column.getValue().stream()
                        .filter(ProjectCellFilter.class::isInstance)
                        .map(
                            filter ->
                                getProjectCellFilterTag(
                                    column.getKey(), (ProjectCellFilter) filter))
                        .collect(toList())))
        .flatMap(column -> column.getValue().stream())
        .collect(Collectors.toList());
  }

  private static Tag getProjectRowFilterTag(ProjectRowFilter filter) {
    return filterToTag(filter.toString(), filter);
  }

  private static Tag getProjectCellFilterTag(String key, ProjectCellFilter filter) {
    return filterToTag(key + filter.toString(), filter);
  }

  private static Tag addPrefixToTag(Tag tag, String prefix) {
    String newTagStr = format("@%s:%s", prefix, tag.getName().substring(1));
    return new Tag(LOCATION, newTagStr, tag.getId());
  }
}

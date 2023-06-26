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

package de.gematik.combine;

import static de.gematik.combine.CombineMojo.ErrorType.MINIMAL_TABLE;
import static de.gematik.combine.CombineMojo.ErrorType.PROPERTY;
import static de.gematik.combine.CombineMojo.ErrorType.SIZE;
import static de.gematik.combine.tags.parser.AllowDoubleLineupTagParser.ALLOW_DOUBLE_LINEUP_TAG;
import static de.gematik.combine.tags.parser.AllowSelfCombineTagParser.ALLOW_SELF_COMBINE_TAG;
import static de.gematik.combine.tags.parser.MinimalTableTagParser.MINIMAL_TABLE_TAG;
import static de.gematik.utils.Utils.getItemsToCombine;
import static de.gematik.utils.Utils.writeErrors;
import static java.lang.String.format;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.io.FileUtils.copyDirectory;
import static org.apache.commons.io.FileUtils.deleteDirectory;
import static org.apache.commons.io.FileUtils.listFiles;
import static org.apache.commons.io.filefilter.DirectoryFileFilter.DIRECTORY;
import static org.apache.commons.io.filefilter.FileFilterUtils.suffixFileFilter;

import de.gematik.BaseMojo;
import de.gematik.combine.execution.FileProcessor;
import de.gematik.combine.model.CombineItem;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;
import javax.inject.Inject;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Plugin for filling empty gherkin tables with generated combinations
 */
@Mojo(name = "prepare-combine", defaultPhase = LifecyclePhase.GENERATE_TEST_SOURCES)
@Setter
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class CombineMojo extends BaseMojo {

  public static final String EMPTY_EXAMPLES_TABLE_TAG = "@EMPTY_EXAMPLES_TABLE";
  public static final String WIP_TAG = "@WIP";
  public static final String TEST_RESOURCES_DIR = "./src/test/resources/";
  public static final String WARN_MESSAGE = "=== Caution!!! The feature file which is prepared have some issues and may not contain the expected value ===";
  public static final String MINIMAL_TABLE_ERROR_HEADER = "Minimal table should be created and failed. For following apis a valid row could not be generated:\n\t";

  @Getter
  @Setter
  private static CombineMojo instance;
  @Getter
  private static List<String> tableSizeErrorLog = new ArrayList<>();
  @Getter
  private static List<String> minimalTableErrorLog = new ArrayList<>();
  @Getter
  private static List<String> propertyErrorLog = new ArrayList<>();
  private final FileProcessor replacer;
  /**
   * Path to the directory where the rendered templates got stored
   */
  @Parameter(property = "outputDir", defaultValue = TEST_RESOURCES_DIR + "features")
  String outputDir;
  /**
   * Path to the directory of the templates
   */
  @Parameter(property = "templateDir", defaultValue = TEST_RESOURCES_DIR + "templates")
  String templateDir;
  /**
   * The specific ending of the templates
   */
  @Parameter(property = "ending", defaultValue = ".cute")
  String ending;
  /**
   * The plugin will add these tags to all examples tables where it was not able to add at least one
   * row
   */
  @Parameter(property = "emptyExamplesTags")
  List<String> emptyExamplesTags;
  /**
   * The plugin will throw exception it was not able to add at least one row
   */
  @Parameter(property = "breakIfTableToSmall", defaultValue = "true")
  boolean breakIfTableToSmall;
  /**
   * The plugin will throw exception it was not able to add at least one row
   */
  @Parameter(property = "minTableSize", defaultValue = "1")
  int minTableSize;
  /**
   * The plugin will throw exception if minimal table could not generate valid row for at least one
   * value
   */
  @Parameter(property = "breakIfMinimalTableError", defaultValue = "false")
  boolean breakIfMinimalTableError;
  /**
   * Prefix that is added to all plugin specific tags (except version filters!) in the feature file
   * to categorize them under in the report
   */
  @Getter
  @Parameter(property = "pluginTagCategory", defaultValue = "Plugin")
  String pluginTagCategory;
  /**
   * Prefix that is added to all version filter tags in the feature file to categorize them under in
   * the report
   */

  @Getter
  @Parameter(property = "versionFilterTagCategory", defaultValue = "VersionFilter")
  String versionFilterTagCategory;
  /**
   * The plugin will look for this property to determine the version of an item, may be set to an
   * arbitrary string, default is "version"
   */
  @Getter
  @Parameter(property = "versionProperty", defaultValue = "version")
  String versionProperty;
  /**
   * List of tags that are added to each processed examples table
   */
  @Parameter(property = "defaultExamplesTags")
  List<String> defaultExamplesTags;
  /**
   * List of tags that are skipped by this plugin
   */
  @Parameter(property = "skipTags")
  List<String> skipTags;
  /**
   * Filter Configuration
   */
  @Parameter(property = "filterConfiguration")
  FilterConfiguration filterConfiguration;
  /**
   * Project Filters
   */
  @Parameter(property = "projectFilters")
  ProjectFilters projectFilters;
  /**
   * Parameter to decide if combine-execution should be run
   */
  @Parameter(name = "skipComb", defaultValue = "false")
  boolean skipComb;

  @SneakyThrows
  public void execute() {
    if (this.isSkip() || skipComb) {
      getLog().warn("Combine items and generate feature files got skipped due configuration");
      return;
    }
    setInstance(this);
    doChecks();
    deleteDirectory(new File(outputDir));
    execute(getConfiguration());
  }

  public void execute(CombineConfiguration config) throws MojoExecutionException {
    String outDir = config.getOutputDir();
    String fileEnding = config.getTemplateFileEnding();

    List<CombineItem> itemsToCombine = getItemsToCombine(
        new File(config.getCombineItemFile()), this, true);

    copyFiles(config.getTemplateDir(), outDir, fileEnding);

    Collection<File> files = allFiles(outDir, fileEnding);
    if (files.isEmpty()) {
      getPluginLog().warn("There are no files to process in " + outDir);
    }

    files.stream()
        .map(file -> stripEnding(file, fileEnding))
        .forEach(file -> replacer.process(file, config, itemsToCombine));
    writeErrors(getClass().getSimpleName(),
        Stream.of(minimalTableErrorLog, tableSizeErrorLog, propertyErrorLog)
            .flatMap(Collection::stream)
            .collect(toList()),
        WARN_MESSAGE, true);
    if (config.isBreakIfTableToSmall() && !tableSizeErrorLog.isEmpty()) {
      throw new MojoExecutionException(
          "Scenarios with insufficient examples found -> \n" + String.join("\n",
              tableSizeErrorLog));
    }
    if (config.isBreakIfMinimalTableError() && !minimalTableErrorLog.isEmpty()) {
      throw new MojoExecutionException(
          MINIMAL_TABLE_ERROR_HEADER + String.join("\n\t", minimalTableErrorLog));
    }
  }

  @SneakyThrows
  private void doChecks() {
    File templateDirFile = new File(templateDir);
    if (!templateDirFile.exists()) {
      throw new MojoExecutionException(
          "Template directory does not exist: " + templateDirFile.getAbsolutePath());
    }
    File file = new File(getCombineItemsFile());
    if (!file.exists() || !file.isFile()) {
      throw new MojoExecutionException("Combine items file not found: " + file.getAbsolutePath());
    }
    defaultExamplesTags.forEach(this::checkDefaultTag);
  }

  @SneakyThrows
  private void checkDefaultTag(String tag) {
    if (!tag.startsWith("@")
        || tag.lastIndexOf("@") > 0
        || tag.trim().contains(" ")
    ) {
      throw new MojoExecutionException(tag + " is not a valid default tag");
    }

    List<String> forbiddenTags = List.of(ALLOW_DOUBLE_LINEUP_TAG, ALLOW_SELF_COMBINE_TAG,
        MINIMAL_TABLE_TAG);
    if (forbiddenTags.stream().anyMatch(tag::contains)) {
      throw new MojoExecutionException(
          format("Default tag '%s' is not allowed to contain configuration tags! %s", tag,
              forbiddenTags));
    }
  }

  private CombineConfiguration getConfiguration() {
    if (emptyExamplesTags.isEmpty()) {
      emptyExamplesTags = List.of(EMPTY_EXAMPLES_TABLE_TAG, WIP_TAG);
    }

    if (skipTags.isEmpty()) {
      skipTags = List.of(WIP_TAG);
    }

    if (pluginTagCategory == null) {
      pluginTagCategory = "Plugin";
    }

    if (versionFilterTagCategory == null) {
      versionFilterTagCategory = "VersionFilter";
    }

    if (versionProperty == null) {
      versionProperty = "version";
    }

    if (nonNull(projectFilters)) {
      projectFilters.parseProjectFilters();
    }

    if (!ending.startsWith(".")) {
      ending = format(".%s", ending);
    }

    return CombineConfiguration.builder()
        .templateDir(templateDir)
        .templateFileEnding(ending)
        .outputDir(outputDir)
        .combineItemFile(getCombineItemsFile())
        .pluginTagCategory(pluginTagCategory)
        .versionFilterTagCategory(versionFilterTagCategory)
        .emptyExamplesTags(emptyExamplesTags)
        .defaultExamplesTags(defaultExamplesTags)
        .skipTags(skipTags.stream().map(String::toLowerCase).collect(toList()))
        .filterConfiguration(filterConfiguration)
        .projectFilters(projectFilters)
        .breakIfTableToSmall(breakIfTableToSmall)
        .minTableSize(minTableSize)
        .breakIfMinimalTableError(breakIfMinimalTableError)
        .build();
  }

  @SneakyThrows
  public static void copyFiles(String from, String to, String ending) {
    File sourceDirectory = new File(from);
    File destinationDirectory = new File(to);

    IOFileFilter fileFilter = suffixFileFilter(ending).or(DIRECTORY);
    try {
      copyDirectory(sourceDirectory, destinationDirectory, fileFilter);
    } catch (FileNotFoundException e) {
      getPluginLog().error(e);
      throw new MojoExecutionException(e);
    }
  }

  public static File stripEnding(File file, String ending) {
    File dest = new File(file.getAbsolutePath().replace(ending, ""));
    boolean success = file.renameTo(dest);
    if (!success) {
      getPluginLog().error(
          "could not rename " + file.getAbsolutePath() + " to " + dest.getAbsolutePath());
    }
    return dest;
  }

  @SneakyThrows
  public static Collection<File> allFiles(String dir, String ending) {
    File inputDir = new File(dir);
    return listFiles(inputDir, new String[]{ending.replace(".", "")}, true);
  }

  public static Log getPluginLog() {
    return getInstance().getLog();
  }

  public static void appendError(String error, ErrorType type) {
    if (type == SIZE) {
      tableSizeErrorLog.add(error);
    } else if (type == MINIMAL_TABLE) {
      minimalTableErrorLog.add(error);
    } else if (type == PROPERTY) {
      propertyErrorLog.add(error);
    }
  }

  public static void resetError() {
    tableSizeErrorLog = new ArrayList<>();
    minimalTableErrorLog = new ArrayList<>();
    propertyErrorLog = new ArrayList<>();
  }

  public enum ErrorType {
    SIZE,
    MINIMAL_TABLE,
    PROPERTY
  }

}

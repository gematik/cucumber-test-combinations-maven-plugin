/*
 * Copyright (c) 2023 gematik GmbH
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
import static io.cucumber.gherkin.utils.pretty.Pretty.prettyPrint;
import static io.cucumber.messages.types.SourceMediaType.TEXT_X_CUCUMBER_GHERKIN_PLAIN;
import static java.nio.file.Files.readString;
import static java.nio.file.Files.writeString;

import de.gematik.combine.CombineConfiguration;
import de.gematik.combine.model.CombineItem;
import io.cucumber.gherkin.GherkinParser;
import io.cucumber.gherkin.utils.pretty.Syntax;
import io.cucumber.messages.types.Envelope;
import io.cucumber.messages.types.GherkinDocument;
import io.cucumber.messages.types.Source;
import java.io.File;
import java.util.List;
import java.util.Optional;
import javax.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.maven.plugin.MojoExecutionException;

@RequiredArgsConstructor(onConstructor_ = @Inject)
public class FileProcessor {

  private final GherkinProcessor gherkinProcessor;

  @SneakyThrows
  public void process(File file, CombineConfiguration config, List<CombineItem> combineItems) {
    getPluginLog().info("processing: " + file.getName());
    getPluginLog().debug("file: " + file.getAbsolutePath());

    final String oldContent = readString(file.toPath());

    final GherkinDocument gherkinDocument;
    try {
      gherkinDocument = parseGherkinString(oldContent);
      getPluginLog().debug("parsed gherkin from: " + file.getAbsolutePath());
    } catch (IllegalArgumentException e) {
      throw new MojoExecutionException(e.getMessage() + " " + file.getAbsolutePath());
    }

    gherkinProcessor.process(gherkinDocument, config, combineItems);

    getPluginLog().debug("writing result to: " + file.getAbsolutePath());
    final String newContent = prettyPrint(gherkinDocument, Syntax.gherkin);
    writeString(file.toPath(), newContent);

    getPluginLog().info("processed: " + file.getName());
  }

  private static GherkinDocument parseGherkinString(String gherkin) {
    final GherkinParser parser = GherkinParser.builder()
        .includeSource(false)
        .includePickles(false)
        .includeGherkinDocument(true)
        .build();

    final Source source = new Source("not needed", gherkin, TEXT_X_CUCUMBER_GHERKIN_PLAIN);
    final Envelope envelope = Envelope.of(source);

    return parser.parse(envelope)
        .map(Envelope::getGherkinDocument)
        .flatMap(Optional::stream)
        .findAny()
        .orElseThrow(
            () -> new IllegalArgumentException("Could not parse invalid gherkin."));
  }

}

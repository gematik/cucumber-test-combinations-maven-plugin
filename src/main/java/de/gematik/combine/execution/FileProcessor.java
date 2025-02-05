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

import static de.gematik.combine.CombineMojo.getPluginLog;
import static io.cucumber.gherkin.utils.pretty.Pretty.prettyPrint;
import static io.cucumber.messages.types.SourceMediaType.TEXT_X_CUCUMBER_GHERKIN_PLAIN;
import static java.nio.file.Files.delete;
import static java.nio.file.Files.readString;
import static java.nio.file.Files.writeString;

import de.gematik.combine.CombineConfiguration;
import de.gematik.combine.CombineMojo;
import de.gematik.combine.model.CombineItem;
import io.cucumber.gherkin.GherkinParser;
import io.cucumber.gherkin.utils.pretty.Syntax;
import io.cucumber.messages.types.Envelope;
import io.cucumber.messages.types.GherkinDocument;
import io.cucumber.messages.types.ParseError;
import io.cucumber.messages.types.Source;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import javax.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@RequiredArgsConstructor(onConstructor_ = @Inject)
public class FileProcessor {

  private final GherkinProcessor gherkinProcessor;

  @SneakyThrows
  public void process(File file, CombineConfiguration config, List<CombineItem> combineItems) {
    getPluginLog().info("processing: " + file.getName());
    getPluginLog().debug("file: " + file.getAbsolutePath());

    final String oldContent = readString(file.toPath());

    GherkinDocument gherkinDocument;
    try {
      gherkinDocument = parseGherkinString(file.toURI().toString(), oldContent);
      getPluginLog().debug("parsed gherkin from: " + file.getAbsolutePath());
    } catch (IllegalArgumentException e) {
      CombineMojo.appendError(e.getMessage(), CombineMojo.ErrorType.WARNING);
      gherkinDocument = null;
    }

    int numberOfScenarios = 0;
    if (gherkinDocument != null) {
      numberOfScenarios = gherkinProcessor.generateExamples(gherkinDocument, config, combineItems);
    }
    if (numberOfScenarios > 0) {
      getPluginLog().debug("writing result to: " + file.getAbsolutePath());
      final String newContent = prettyPrint(gherkinDocument, Syntax.gherkin);
      writeString(file.toPath(), newContent);
    } else {
      getPluginLog().warn("No scenarios to process in file: " + file.getName());
      delete(file.toPath());
    }

    getPluginLog().info("processed: " + file.getName());
  }

  public static GherkinDocument parseGherkinString(String uri, String gherkin) {
    final GherkinParser parser =
        GherkinParser.builder()
            .includeSource(false)
            .includePickles(false)
            .includeGherkinDocument(true)
            .build();

    final Source source = new Source(uri, gherkin, TEXT_X_CUCUMBER_GHERKIN_PLAIN);
    final Envelope envelope = Envelope.of(source);

    return parser
        .parse(envelope)
        .map(FileProcessor::getGherkinDocument)
        .findAny()
        .orElseThrow(() -> new IllegalArgumentException("Could not parse invalid gherkin."));
  }

  private static GherkinDocument getGherkinDocument(Envelope envelope) {
    return envelope
        .getGherkinDocument()
        .orElseThrow(
            () ->
                new IllegalArgumentException(
                    "Could not parse invalid gherkin: " + getParseErrorMessage(envelope)));
  }

  private static String getParseErrorMessage(Envelope envelope) {
    return envelope
        .getParseError()
        .map(error -> getSourcePrefix(error) + error.getMessage())
        .orElse("unknown error");
  }

  private static String getSourcePrefix(ParseError error) {
    return error
        .getSource()
        .getUri()
        .map(
            uri -> {
              try {
                return new URI(uri).getPath() + ": ";
              } catch (URISyntaxException e) {
                return "";
              }
            })
        .orElse("");
  }
}

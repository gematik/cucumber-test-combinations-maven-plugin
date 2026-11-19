/*
 * Copyright (Change Date see Readme), gematik GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * ******
 *
 * For additional notes and disclaimer from gematik and in case of changes by gematik find details in the "Readme" file.
 */

package de.gematik;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import de.gematik.check.CheckMojo;
import de.gematik.combine.CombineMojo;
import de.gematik.prepare.PrepareItemsMojo;
import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import javax.xml.parsers.DocumentBuilderFactory;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

class BaseMojoTest {

  @Test
  void shouldResolveGeneratedDirInsideBuildDirectory() {
    File buildDirectory = new File("targetCustom");
    BaseMojo mojo = new CombineMojo(null);
    mojo.setBuildDirectory(buildDirectory);

    assertThat(mojo.getGeneratedCombineItemsDir())
        .isEqualTo(new File(buildDirectory, "generated-combine"));
  }

  @Test
  void shouldDefaultToTargetGeneratedCombine() {
    assertThat(new PrepareItemsMojo(null, null).getGeneratedCombineItemsDir())
        .isEqualTo(new File("target", "generated-combine"));
    assertThat(new CheckMojo(null, null).getGeneratedCombineItemsDir())
        .isEqualTo(new File("target", "generated-combine"));
  }

  @Test
  @SneakyThrows
  void pluginDescriptorShouldInjectProjectBuildDirectoryForAllGoals() {
    InputStream descriptor =
        getClass().getClassLoader().getResourceAsStream("META-INF/maven/plugin.xml");
    assumeTrue(descriptor != null, "plugin descriptor not generated (not built by maven)");

    Document doc;
    try (descriptor) {
      doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(descriptor);
    }
    NodeList mojos = doc.getElementsByTagName("mojo");
    Map<String, String> buildDirByGoal = new HashMap<>();
    for (int i = 0; i < mojos.getLength(); i++) {
      Element mojo = (Element) mojos.item(i);
      String goal = mojo.getElementsByTagName("goal").item(0).getTextContent();
      NodeList configuration = mojo.getElementsByTagName("buildDirectory");
      for (int j = 0; j < configuration.getLength(); j++) {
        Element element = (Element) configuration.item(j);
        if (element.hasAttribute("default-value")) {
          buildDirByGoal.put(goal, element.getAttribute("default-value"));
        }
      }
    }

    assertThat(buildDirByGoal)
        .containsEntry("prepare-items", "${project.build.directory}")
        .containsEntry("prepare-combine", "${project.build.directory}")
        .containsEntry("check", "${project.build.directory}");
  }
}

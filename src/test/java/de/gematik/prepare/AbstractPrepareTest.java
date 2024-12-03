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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.spy;

import de.gematik.utils.request.ApiRequester;
import lombok.SneakyThrows;
import org.apache.maven.plugin.logging.Log;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public abstract class AbstractPrepareTest {

  @Mock ItemsCreator itemsCreator;

  @Mock ApiRequester apiRequester;

  PrepareItemsMojo mojo;

  @Mock Log log;

  @BeforeEach
  @SneakyThrows
  void setup() {
    mojo = spy(new PrepareItemsMojo(apiRequester));
    lenient().when(mojo.getLog()).thenReturn(log);
    mojo.setItemsCreator(itemsCreator);
    mojo.setEnvVarFormat("");
    lenient().doReturn("{}").when(apiRequester).getApiResponse(any());
    PrepareItemsMojo.setInstance(mojo);
  }
}

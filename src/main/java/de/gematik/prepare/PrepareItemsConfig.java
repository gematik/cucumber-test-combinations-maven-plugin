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

import de.gematik.prepare.pooling.GroupMatchStrategyType;
import de.gematik.prepare.pooling.PoolGroup;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PrepareItemsConfig {

  private String combineItemsFile;

  private String infoResourceLocation;

  private List<TagExpression> tagExpressions;

  private List<PropertyExpression> propertyExpressions;

  private List<PoolGroup> poolGroups;

  private List<String> excludedGroups;

  private int poolSize;

  private GroupMatchStrategyType defaultMatchStrategy;

  private String acceptedResponseFamilies;

  private String allowedResponseCodes;
}

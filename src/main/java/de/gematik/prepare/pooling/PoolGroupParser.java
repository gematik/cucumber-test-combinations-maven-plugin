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

package de.gematik.prepare.pooling;

import de.gematik.prepare.pooling.PoolGroup.PoolGroupBuilder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

public class PoolGroupParser {

  public List<PoolGroup> transformStringToGroups(String poolGroupString) {
    String[] splitted = poolGroupString.split(";");
    List<PoolGroup> poolGroups = new ArrayList<>();
    for (String s : splitted) {
      String[] groupSplit = s.split(",");
      PoolGroupBuilder poolGroup =
          PoolGroup.builder().groupPattern(Arrays.asList(groupSplit[0].split("\\|")));
      if (groupSplit.length > 1 && StringUtils.isNotBlank(groupSplit[1])) {
        poolGroup.amount(Integer.parseInt(groupSplit[1]));
      }
      if (groupSplit.length > 2 && StringUtils.isNotBlank(groupSplit[2])) {
        poolGroup.strategy(GroupMatchStrategyType.valueOf(groupSplit[2]));
      }
      poolGroups.add(poolGroup.build());
    }
    return poolGroups;
  }
}

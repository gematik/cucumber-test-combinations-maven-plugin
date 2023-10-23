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

package de.gematik.combine.filter.jexl;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.List;
import lombok.experimental.Delegate;

public class JexlFilterList<T> {

  @Delegate
  private final List<T> list;

  public JexlFilterList() {
    list = new ArrayList<>();
  }

  public JexlFilterList(List<T> list) {
    this.list = new ArrayList<>(list);
  }

  // method is used in JEXL expressions
  public JexlFilterList<T> distinct() {
    return new JexlFilterList<>(list.stream()
        .distinct()
        .collect(toList()));
  }

  // method is used in JEXL expressions
  public int count() {
    return list.size();
  }

  // method is used in JEXL expressions
  public int count(T element) {
    return (int) list.stream()
        .filter(x -> x.equals(element))
        .count();
  }
}

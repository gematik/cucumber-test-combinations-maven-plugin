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

package de.gematik.combine.util;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class NonNullableMap<K, V> implements Map<K, V> {

  private final Map<K, V> innerMap;
  private final Function<K, V> defaultValue;

  public static <K, V> NonNullableMap<K, V> nonNullableMap(Map<K, V> innerMap,
      Function<K, V> defaultValue) {
    return new NonNullableMap<>(innerMap, defaultValue);
  }

  @Override
  @SuppressWarnings("unchecked")
  public V get(Object key) {
    V value = innerMap.get(key);
    if (value == null) {
      return defaultValue.apply((K) key);
    }
    return value;
  }

  @Override
  public int size() {
    return innerMap.size();
  }

  @Override
  public boolean isEmpty() {
    return innerMap.isEmpty();
  }

  @Override
  public boolean containsKey(Object key) {
    return innerMap.containsKey(key);
  }

  @Override
  public boolean containsValue(Object value) {
    return innerMap.containsValue(value);
  }

  @Override
  public V put(K key, V value) {
    return innerMap.put(key, value);
  }

  @Override
  public V remove(Object key) {
    return innerMap.remove(key);
  }

  @Override
  public void putAll(Map<? extends K, ? extends V> map) {
    innerMap.putAll(map);
  }

  @Override
  public void clear() {
    innerMap.clear();
  }

  @Override
  public Set<K> keySet() {
    return innerMap.keySet();
  }

  @Override
  public Collection<V> values() {
    return innerMap.values();
  }

  @Override
  public Set<Entry<K, V>> entrySet() {
    return innerMap.entrySet();
  }
}
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

package de.gematik.prepare.pooling;

public enum GroupMatchStrategyType {

  // tag::matchingStrategies[]
  REGEX, // All entries will be compiled as regex
  WILDCARD, // "*" can be used as Wildcard e.g. My*Group* would match everything that starts with "My" and contains "Group" somewhere. Is Everytime CASE insensitive
  CASE_SENSITIVE, // Proof if the named group is a substring of a group in item (case sensitive)
  CASE_INSENSITIVE, // Proof if the named group is a substring of a group in item (case insensitive)
  CASE_SENSITIVE_EXACT, // Proof if the named group have a exact match with a group in item (case sensitive)
  CASE_INSENSITIVE_EXACT // Proof if the named group have a exact match with a group in item (case insensitive)
  // end::matchingStrategies[]
}

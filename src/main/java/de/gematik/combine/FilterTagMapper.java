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

import static java.lang.String.format;

import de.gematik.combine.filter.project.ProjectVersionFilter;
import de.gematik.combine.filter.table.DistinctColumnFilter;
import de.gematik.combine.filter.table.DoubleLineupFilter;
import de.gematik.combine.filter.table.MaxRowsFilter;
import de.gematik.combine.filter.table.MaxSameColumnPropertyFilter;
import de.gematik.combine.filter.table.ShuffleTableFilter;
import de.gematik.combine.filter.table.cell.JexlCellFilter;
import de.gematik.combine.filter.table.cell.VersionFilter;
import de.gematik.combine.filter.table.row.DistinctRowPropertyFilter;
import de.gematik.combine.filter.table.row.EqualRowPropertyFilter;
import de.gematik.combine.filter.table.row.JexlRowFilter;
import de.gematik.combine.filter.table.row.RequireTagRowFilter;
import de.gematik.combine.filter.table.row.SelfCombineFilter;
import de.gematik.combine.tags.parser.AllowDoubleLineupTagParser;
import de.gematik.combine.tags.parser.AllowSelfCombineTagParser;
import de.gematik.combine.tags.parser.DistinctColumnTagParser;
import de.gematik.combine.tags.parser.DistinctRowPropertyTagParser;
import de.gematik.combine.tags.parser.EqualRowPropertyTagParser;
import de.gematik.combine.tags.parser.FilterTagParser;
import de.gematik.combine.tags.parser.MaxRowsTagParser;
import de.gematik.combine.tags.parser.MaxSameColumnPropertyTagParser;
import de.gematik.combine.tags.parser.RequireTagTagParser;
import de.gematik.combine.tags.parser.ShuffleTagParser;
import de.gematik.combine.tags.parser.VersionFilterParser;
import io.cucumber.messages.types.Location;
import io.cucumber.messages.types.Tag;
import java.util.HashMap;
import java.util.Map;
import lombok.NonNull;

public final class FilterTagMapper {

  private static final Map<Class<?>, String> MAP = new HashMap<>();

  static {
    MAP.put(JexlCellFilter.class, FilterTagParser.JEXL_ROW_FILTER_TAG);
    MAP.put(JexlRowFilter.class, FilterTagParser.JEXL_ROW_FILTER_TAG);
    MAP.put(VersionFilter.class, VersionFilterParser.VERSION_TAG);
    MAP.put(ProjectVersionFilter.class, VersionFilterParser.VERSION_TAG);
    MAP.put(DistinctRowPropertyFilter.class,
        DistinctRowPropertyTagParser.DISTINCT_ROW_PROPERTY_TAG);
    MAP.put(EqualRowPropertyFilter.class, EqualRowPropertyTagParser.EQUAL_ROW_PROPERTY_TAG);
    MAP.put(RequireTagRowFilter.class, RequireTagTagParser.REQUIRE_TAG_TAG);
    MAP.put(SelfCombineFilter.class, AllowSelfCombineTagParser.ALLOW_SELF_COMBINE_TAG);
    MAP.put(DistinctColumnFilter.class, DistinctColumnTagParser.DISTINCT_COLUMN_TAG);
    MAP.put(DoubleLineupFilter.class, AllowDoubleLineupTagParser.ALLOW_DOUBLE_LINEUP_TAG);
    MAP.put(MaxRowsFilter.class, MaxRowsTagParser.MAX_ROWS_TAG);
    MAP.put(MaxSameColumnPropertyFilter.class,
        MaxSameColumnPropertyTagParser.MAX_SAME_COLUMN_PROPERTY_TAG);
    MAP.put(ShuffleTableFilter.class, ShuffleTagParser.SHUFFLE_TAG);
  }

  private FilterTagMapper() {
  }

  @NonNull
  public static String getTagName(Class<?> clazz) {
    return MAP.get(clazz);
  }

  public static Tag filterToTag(String value, Object caller) {
    String s = format("@%s(%s)",
        (caller instanceof String) ? caller : getTagName(caller.getClass()), value);
    return new Tag(new Location(0L, 0L), s, s);
  }
}

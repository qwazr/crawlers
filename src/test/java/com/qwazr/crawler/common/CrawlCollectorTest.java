/*
 * Copyright 2017-2020 Emmanuel Keller / QWAZR
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qwazr.crawler.common;

import com.qwazr.utils.IfNotNull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CrawlCollectorTest<ITEM extends CrawlItem<?>> implements CrawlCollector<ITEM> {

    public static final List<CrawlItem<?>> all = new ArrayList<>();
    public static final Map<Object, Rejected> rejecteds = new HashMap<>();
    public static final Map<Object, String> errors = new HashMap<>();
    public static final Map<Integer, List<Object>> depths = new LinkedHashMap<>();

    public static void resetCounters() {
        all.clear();
        rejecteds.clear();
        depths.clear();
    }

    @Override
    public void collect(final ITEM crawlItem) {
        all.add(crawlItem);
        IfNotNull.apply(crawlItem.getRejected(), r -> rejecteds.put(crawlItem.getItem(), r));
        IfNotNull.apply(crawlItem.getError(), e -> errors.put(crawlItem.getItem(), e));
        depths.computeIfAbsent(crawlItem.getDepth(), d -> new ArrayList<>()).add(crawlItem.getItem());
    }

    public static <T> List<T> getAll(Class<T> itemClass) {
        return all.stream().map(crawlItem -> itemClass.cast(crawlItem.getItem())).collect(Collectors.toList());
    }

}

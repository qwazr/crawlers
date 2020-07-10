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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CrawlCollectorTest<ITEM extends CrawlItem> implements CrawlCollector<ITEM> {

    public static final List<CrawlItem> count = new ArrayList<>();
    public static final List<CrawlItem> crawled = new ArrayList<>();
    public static final List<CrawlItem> ignored = new ArrayList<>();
    public static final List<CrawlItem> inInclusion = new ArrayList<>();
    public static final List<CrawlItem> inExclusion = new ArrayList<>();
    public static final Map<Integer, List<CrawlItem>> depths = new LinkedHashMap<>();

    public static void resetCounters() {
        count.clear();
        crawled.clear();
        ignored.clear();
        inInclusion.clear();
        inExclusion.clear();
        depths.clear();
    }

    @Override
    public void collect(final ITEM crawlItem) {
        count.add(crawlItem);
        if (crawlItem.isCrawled())
            crawled.add(crawlItem);
        if (crawlItem.isIgnored())
            ignored.add(crawlItem);
        if (Boolean.TRUE == crawlItem.isInExclusion())
            inExclusion.add(crawlItem);
        if (Boolean.TRUE == crawlItem.isInInclusion())
            inInclusion.add(crawlItem);
        depths.computeIfAbsent(crawlItem.getDepth(), d -> new ArrayList<>()).add(crawlItem);
    }

}

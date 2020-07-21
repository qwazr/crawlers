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
package com.qwazr.crawler.file;

import com.qwazr.crawler.common.Attributes;
import com.qwazr.crawler.common.CrawlCollector;
import com.qwazr.crawler.common.CrawlCollectorTest;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import javax.validation.constraints.NotNull;
import org.junit.Assert;

public class FileCrawlCollectorFactoryTest implements FileCrawlCollectorFactory {

    public static final AtomicReference<Attributes> attributes = new AtomicReference<>();
    public static final AtomicReference<FileCrawlDefinition> definition = new AtomicReference<>();
    public static final List<Path> paths = new ArrayList<>();
    public static final Map<Path, Integer> pathDepth = new LinkedHashMap<>();
    public static final Map<Path, String> pathError = new LinkedHashMap<>();

    public static void resetCounters() {
        definition.set(null);
        CrawlCollectorTest.resetCounters();
        paths.clear();
        pathDepth.clear();
        pathError.clear();
    }

    @Override
    public @NotNull CrawlCollector<FileCrawlItem> createCrawlCollector(final Attributes attributes,
                                                                       final FileCrawlDefinition crawlDefinition) {
        FileCrawlCollectorFactoryTest.attributes.set(attributes);
        definition.set(crawlDefinition);
        return new FileCrawlCollectorTest();
    }

    public static class FileCrawlCollectorTest extends CrawlCollectorTest<FileCrawlItem> {

        @Override
        public void collect(final FileCrawlItem crawlItem) {
            super.collect(crawlItem);
            Assert.assertNotNull(crawlItem.getItem());
            Assert.assertNotNull(crawlItem.getAttributes());
            paths.add(crawlItem.getItem());
            if (crawlItem.getError() != null)
                pathError.put(crawlItem.getItem(), crawlItem.getError());
            pathDepth.put(crawlItem.getItem(), crawlItem.getDepth());
        }

    }


}

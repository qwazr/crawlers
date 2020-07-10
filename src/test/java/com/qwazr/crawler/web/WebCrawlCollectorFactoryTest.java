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
package com.qwazr.crawler.web;

import com.qwazr.crawler.common.CrawlCollector;
import com.qwazr.crawler.common.CrawlCollectorTest;
import com.qwazr.utils.IOUtils;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import javax.validation.constraints.NotNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.IsIterableContaining.hasItem;
import org.junit.Assert;

public class WebCrawlCollectorFactoryTest implements WebCrawlCollectorFactory {

    public static final AtomicReference<WebCrawlDefinition> definition = new AtomicReference<>();
    public static final List<URI> uris = new ArrayList<>();
    public static final Map<URI, Integer> uriDepth = new LinkedHashMap<>();
    public static final Map<URI, String> uriError = new LinkedHashMap<>();
    public static final Map<URI, Integer> statusCodes = new LinkedHashMap<>();
    public static final Map<URI, Integer> contentSizes = new LinkedHashMap<>();
    public static final Map<URI, URI> redirects = new LinkedHashMap<>();
    public static final List<URI> robotsTxtRejected = new ArrayList<>();

    public static void resetCounters() {
        definition.set(null);
        CrawlCollectorTest.resetCounters();
        uris.clear();
        uriDepth.clear();
        uriError.clear();
        statusCodes.clear();
        contentSizes.clear();
        redirects.clear();
        robotsTxtRejected.clear();
    }

    public static void checkUri(final String uriString, Integer depth, Integer statusCode, Integer contentSize) {
        final URI uri = URI.create(uriString);
        if (depth == null)
            assertThat(uriDepth.keySet(), not(hasItem(uri)));
        else
            assertThat(uriDepth, hasEntry(uri, depth));
        if (statusCode == null)
            assertThat(statusCodes.keySet(), not(hasItem(uri)));
        else
            assertThat(statusCodes, hasEntry(uri, statusCode));
        if (contentSize == null)
            assertThat(contentSizes.keySet(), not(hasItem(uri)));
        else
            assertThat(contentSizes, hasEntry(uri, contentSize));
    }

    @Override
    public @NotNull CrawlCollector<WebCrawlItem> createCrawlCollector(final WebCrawlDefinition crawlDefinition) {
        definition.set(crawlDefinition);
        return new WebCrawlCollectorTest();
    }

    public static class WebCrawlCollectorTest extends CrawlCollectorTest<WebCrawlItem> {

        @Override
        public void collect(final WebCrawlItem crawlItem) {
            super.collect(crawlItem);
            final URI uri = crawlItem.getItem();
            Assert.assertNotNull(uri);
            uris.add(uri);
            if (crawlItem.getError() != null)
                uriError.put(uri, crawlItem.getError());
            uriDepth.put(uri, crawlItem.getDepth());
            if (crawlItem.getStatusCode() != null)
                statusCodes.put(uri, crawlItem.getStatusCode());
            if (crawlItem.getBody() != null) {
                try {
                    contentSizes.put(uri, IOUtils.toByteArray(crawlItem.getBody().getContent().getInput()).length);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            if (crawlItem.getRedirect() != null)
                redirects.put(uri, crawlItem.getRedirect());
            if (Boolean.TRUE == crawlItem.isRobotsTxtDisallow())
                robotsTxtRejected.add(uri);
        }

    }


}

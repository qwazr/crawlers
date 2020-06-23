/*
 * Copyright 2017 Emmanuel Keller / QWAZR
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

import com.qwazr.crawler.file.FileCrawlDefinition;
import com.qwazr.crawler.file.FileCrawlStatus;
import com.qwazr.crawler.web.WebCrawlDefinition;
import com.qwazr.crawler.web.WebCrawlStatus;
import com.qwazr.utils.ObjectMappers;
import com.qwazr.utils.RandomUtils;
import com.qwazr.utils.TimeTracker;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class CrawlStatusTest {

    <T extends CrawlDefinition, S extends CrawlStatus<T, S>> void checkBuilderAndGetter(
            final CrawlStatus.AbstractBuilder<T, S, ?> crawlStatus, final String nodeAddress,
            final TimeTracker timeTracker, final T crawlDefinition, Class<S> crawlStatusClass) throws IOException {

        final String lastError = RandomUtils.alphanumeric(20);
        final String currentCrawl = RandomUtils.alphanumeric(16);
        final Integer currentDepth = RandomUtils.nextInt(1, 100);
        final String abortingReason = RandomUtils.alphanumeric(25);

        final CrawlStatus<T, S> status = crawlStatus.incError()
                .lastError(lastError)
                .crawl(currentCrawl, currentDepth)
                .abort(abortingReason)
                .incCrawled()
                .incCrawled()
                .incCrawled()
                .incIgnored()
                .incIgnored()
                .incRedirect()
                .done()
                .build(true);

        Assert.assertEquals(crawlDefinition, status.getCrawlDefinition());
        Assert.assertEquals(nodeAddress, status.getNodeAddress());
        Assert.assertEquals(timeTracker.getStatus(), status.getTimer());
        Assert.assertEquals(lastError, status.getLastError());
        Assert.assertEquals(currentCrawl, status.getCurrentCrawl());
        Assert.assertEquals(currentDepth, status.getCurrentDepth());
        Assert.assertEquals(abortingReason, status.getAbortingReason());
        Assert.assertEquals(true, status.getAborting());
        Assert.assertEquals(3, status.getCrawled());
        Assert.assertEquals(2, status.getIgnored());
        Assert.assertEquals(1, status.getRedirect());
        Assert.assertEquals(1, status.getError());
        Assert.assertNotNull(status.getStartTime());
        Assert.assertNotNull(status.getEndTime());

        final CrawlStatus<T, S> status2 =
                ObjectMappers.JSON.readValue(ObjectMappers.JSON.writeValueAsString(status), crawlStatusClass);
        Assert.assertEquals(status, status2);
    }

    @Test
    public void testWebCrawlStatus() throws IOException {
        final WebCrawlDefinition crawlDefinition =
                WebCrawlDefinition.of().variable(RandomUtils.alphanumeric(5), RandomUtils.alphanumeric(6)).build();
        final String nodeAddress = RandomUtils.alphanumeric(10);
        final TimeTracker timeTracker = TimeTracker.withDurations();
        checkBuilderAndGetter(WebCrawlStatus.of(nodeAddress, timeTracker, crawlDefinition), nodeAddress, timeTracker,
                crawlDefinition, WebCrawlStatus.class);
    }

    @Test
    public void testFileCrawlStatus() throws IOException {
        final FileCrawlDefinition crawlDefinition =
                FileCrawlDefinition.of().variable(RandomUtils.alphanumeric(5), RandomUtils.alphanumeric(6)).build();
        final String nodeAddress = RandomUtils.alphanumeric(10);
        final TimeTracker timeTracker = TimeTracker.withDurations();
        checkBuilderAndGetter(FileCrawlStatus.of(nodeAddress, timeTracker, crawlDefinition), nodeAddress, timeTracker,
                crawlDefinition, FileCrawlStatus.class);
    }

}

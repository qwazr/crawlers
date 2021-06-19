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

import com.qwazr.crawler.file.FileCrawlSessionStatus;
import com.qwazr.crawler.ftp.FtpCrawlSessionStatus;
import com.qwazr.crawler.web.WebCrawlSessionStatus;
import com.qwazr.utils.ObjectMappers;
import com.qwazr.utils.RandomUtils;
import java.io.IOException;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

public class CrawlStatusTest {

    <STATUS extends CrawlSessionStatus<STATUS>> void checkBuilderAndGetter(
            final CrawlSessionStatus.AbstractBuilder<STATUS, ?> crawlStatus,
            final String nodeAddress,
            final Class<STATUS> crawlStatusClass) throws IOException {

        final String lastError = RandomUtils.alphanumeric(20);
        final String currentCrawl = RandomUtils.alphanumeric(16);
        final Integer currentDepth = RandomUtils.nextInt(1, 100);
        final String abortingReason = RandomUtils.alphanumeric(25);

        final CrawlSessionStatus<STATUS> status = crawlStatus.incError()
                .start()
                .lastError(lastError)
                .crawl(currentCrawl, currentDepth)
                .abort(abortingReason)
                .incCrawled()
                .incCrawled()
                .incCrawled()
                .incRejected()
                .incRejected()
                .incRedirect()
                .done()
                .build();

        Assert.assertEquals(nodeAddress, status.getNodeAddress());
        Assert.assertEquals(lastError, status.getLastError());
        Assert.assertEquals(currentCrawl, status.getCurrentCrawl());
        Assert.assertEquals(currentDepth, status.getCurrentDepth());
        Assert.assertEquals(abortingReason, status.getAbortingReason());
        Assert.assertEquals(true, status.getAborting());
        Assert.assertEquals(3, status.getCrawled());
        Assert.assertEquals(2, status.getRejected());
        Assert.assertEquals(1, status.getRedirect());
        Assert.assertEquals(1, status.getError());
        Assert.assertNotNull(status.getStartTime());
        Assert.assertNotNull(status.getEndTime());

        final CrawlSessionStatus<STATUS> status2 =
                ObjectMappers.JSON.readValue(ObjectMappers.JSON.writeValueAsString(status), crawlStatusClass);
        Assert.assertEquals(status, status2);
    }

    @Test
    public void testWebCrawlSessionStatus() throws IOException {
        final String nodeAddress = RandomUtils.alphanumeric(10);
        checkBuilderAndGetter(WebCrawlSessionStatus.of(nodeAddress),
                nodeAddress, WebCrawlSessionStatus.class);
    }

    @Test
    public void testFileCrawlSessionStatus() throws IOException {
        final String nodeAddress = RandomUtils.alphanumeric(10);
        checkBuilderAndGetter(FileCrawlSessionStatus.of(nodeAddress),
                nodeAddress, FileCrawlSessionStatus.class);
    }

    @Test
    public void testFtpCrawlSessionStatus() throws IOException {
        final String nodeAddress = RandomUtils.alphanumeric(10);
        checkBuilderAndGetter(FtpCrawlSessionStatus.of(nodeAddress),
                nodeAddress, FtpCrawlSessionStatus.class);
    }

}

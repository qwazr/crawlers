/*
 * Copyright 2015-2020 Emmanuel Keller / QWAZR
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apace.org/licenses/LICENSE-2.0
 * <p>h
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qwazr.crawler.web;

import com.qwazr.cluster.ClusterManager;
import com.qwazr.crawler.common.CrawlCollector;
import com.qwazr.crawler.common.CrawlManager;
import com.qwazr.utils.LoggerUtils;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.logging.Logger;

public class WebCrawlerManager extends CrawlManager
        <WebCrawlerManager, WebCrawlThread, WebCrawlSession, WebCrawlDefinition, WebCrawlSessionStatus, WebCrawlItem> {

    private static final Logger LOGGER = LoggerUtils.getLogger(WebCrawlerManager.class);

    private final WebCrawlerServiceInterface service;

    public WebCrawlerManager(final Path crawlerRootDirectory,
                             final String myAddress,
                             final ExecutorService sessionExecutorService,
                             final ExecutorService crawlExecutorService) throws IOException {
        super(crawlerRootDirectory, myAddress, sessionExecutorService, crawlExecutorService, LOGGER,
                WebCrawlSessionStatus.class, WebCrawlDefinition.class);
        service = new WebCrawlerServiceImpl(this);
    }

    public WebCrawlerManager(final Path crawlerRootDirectory,
                             final ClusterManager clusterManager,
                             final ExecutorService sessionExecutorService,
                             final ExecutorService crawlExecutorService) throws IOException {
        this(crawlerRootDirectory, clusterManager.getService().getStatus().me, sessionExecutorService, crawlExecutorService);
    }

    public WebCrawlerServiceInterface getService() {
        return service;
    }

    protected WebCrawlSessionStatus newInitialStatus() {
        return WebCrawlSessionStatus.of(myAddress).build();
    }

    @Override
    protected WebCrawlThread newCrawlThread(final String sessionName,
                                            final WebCrawlDefinition crawlDefinition) {
        final WebCrawlSessionStatus.Builder crawlStatusBuilder = WebCrawlSessionStatus.of(myAddress);
        final CrawlCollector<WebCrawlItem> crawlCollector = newCrawlCollector(crawlDefinition, WebCrawlCollectorFactory.class);
        final WebCrawlSession session = new WebCrawlSession(sessionName, this,
                crawlDefinition, crawlStatusBuilder, crawlCollector == null ? doNothing : crawlCollector);
        return new WebCrawlThread(this, session, crawlDefinition);
    }

    static final CrawlCollector<WebCrawlItem> doNothing = new CrawlCollector<>() {
        @Override
        public void collect(WebCrawlItem crawlItem) {
        }

        @Override
        public void done() {
        }
    };

}

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
import com.qwazr.crawler.common.CrawlManager;
import com.qwazr.scripts.ScriptManager;
import com.qwazr.utils.LoggerUtils;
import com.qwazr.utils.TimeTracker;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.logging.Logger;

public class WebCrawlerManager extends CrawlManager
        <WebCrawlerManager, WebCrawlThread, WebCrawlSession, WebCrawlDefinition, WebCrawlStatus, WebCrawlItem> {

    private static final Logger LOGGER = LoggerUtils.getLogger(WebCrawlerManager.class);

    private final WebCrawlerServiceInterface service;

    public WebCrawlerManager(final Path crawlerRootDirectory,
                             final String myAddress,
                             final ScriptManager scriptManager,
                             final ExecutorService executor) throws IOException {
        super(crawlerRootDirectory, myAddress, scriptManager, executor, LOGGER, WebCrawlStatus.class);
        service = new WebCrawlerServiceImpl(this);
    }

    public WebCrawlerManager(final Path crawlerRootDirectory,
                             final ClusterManager clusterManager,
                             final ScriptManager scriptManager,
                             final ExecutorService executorService) throws IOException {
        this(crawlerRootDirectory, clusterManager.getService().getStatus().me, scriptManager, executorService);
    }

    public WebCrawlerServiceInterface getService() {
        return service;
    }

    @Override
    protected WebCrawlThread newCrawlThread(final String sessionName,
                                            final WebCrawlDefinition crawlDefinition) {
        final TimeTracker timeTracker = TimeTracker.withDurations();
        final WebCrawlStatus.Builder crawlStatusBuilder = WebCrawlStatus.of(myAddress, timeTracker);
        final WebCrawlCollectorFactory collectorFactory = newCrawlCollectorFactory(crawlDefinition, WebCrawlCollectorFactory.class);
        final WebCrawlSession session = new WebCrawlSession(sessionName, this, timeTracker,
                crawlDefinition, attributes, crawlStatusBuilder, collectorFactory);
        return new WebCrawlThread(this, session, crawlDefinition);
    }

}

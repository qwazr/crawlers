/*
 * Copyright 2015-2019 Emmanuel Keller / QWAZR
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

import java.util.concurrent.ExecutorService;
import java.util.logging.Logger;

public class WebCrawlerManager extends CrawlManager<WebCrawlThread, WebCrawlDefinition, WebCrawlStatus> {

    private static final Logger LOGGER = LoggerUtils.getLogger(WebCrawlerManager.class);

    private final WebCrawlerServiceInterface service;

    public WebCrawlerManager(final String myAddress, final ScriptManager scriptManager,
                             final ExecutorService executor) {
        super(myAddress, scriptManager, executor, LOGGER);
        service = new WebCrawlerServiceImpl(this);
    }

    public WebCrawlerManager(ClusterManager clusterManager, ScriptManager scriptManager,
                             ExecutorService executorService) {
        this(clusterManager.getService().getStatus().me, scriptManager, executorService);
    }

    public WebCrawlerServiceInterface getService() {
        return service;
    }

    @Override
    protected WebCrawlThread newCrawlThread(String sessionName, WebCrawlDefinition webCrawlDefinition) {
        final TimeTracker timeTracker = TimeTracker.withDurations();
        final WebCrawlStatus.Builder crawlStatusBuilder = WebCrawlStatus.of(myAddress, timeTracker, webCrawlDefinition);
        final WebCrawlSession session =
                new WebCrawlSession(sessionName, timeTracker, webCrawlDefinition, attributes, crawlStatusBuilder);
        return new WebCrawlThread(this, session, webCrawlDefinition);
    }

}

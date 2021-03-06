/*
 * Copyright 2017-2021 Emmanuel Keller / QWAZR
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

import com.qwazr.cluster.ClusterManager;
import com.qwazr.crawler.common.CrawlCollector;
import com.qwazr.crawler.common.CrawlManager;
import com.qwazr.utils.LoggerUtils;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.logging.Logger;

public class FileCrawlerManager extends CrawlManager
        <FileCrawlerManager, FileCrawlThread, FileCrawlSession, FileCrawlDefinition, FileCrawlSessionStatus, FileCrawlItem> {

    private static final Logger LOGGER = LoggerUtils.getLogger(FileCrawlerManager.class);
    private final FileCrawlerServiceInterface service;

    public FileCrawlerManager(final Path crawlerRootDirectory,
                              final String myAddress,
                              final ExecutorService sessionExecutorService,
                              final ExecutorService crawlExecutorService) throws IOException {
        super(crawlerRootDirectory, myAddress, sessionExecutorService, crawlExecutorService,
                LOGGER, FileCrawlSessionStatus.class, FileCrawlDefinition.class);
        service = new FileCrawlerServiceImpl(this);
    }

    public FileCrawlerManager(final Path crawlerRootDirectory,
                              final ClusterManager clusterManager,
                              final ExecutorService sessionExecutorService,
                              final ExecutorService crawlExecutorService) throws IOException {
        this(crawlerRootDirectory, clusterManager.getService().getStatus().me, sessionExecutorService, crawlExecutorService);
    }

    public FileCrawlerManager(final Path crawlerRootDirectory,
                              final ExecutorService sessionExecutorService,
                              final ExecutorService crawlExecutorService) throws IOException {
        this(crawlerRootDirectory, (String) null, sessionExecutorService, crawlExecutorService);
    }

    public FileCrawlerServiceInterface getService() {
        return service;
    }

    @Override
    protected FileCrawlSessionStatus newInitialStatus() {
        return FileCrawlSessionStatus.of(myAddress).build();
    }

    @Override
    protected FileCrawlThread newCrawlThread(final String sessionName, final FileCrawlDefinition crawlDefinition) {
        final CrawlCollector<FileCrawlItem> crawlCollector = newCrawlCollector(crawlDefinition, FileCrawlCollectorFactory.class);
        final FileCrawlSessionStatus.Builder crawlStatusBuilder = FileCrawlSessionStatus.of(myAddress);
        final FileCrawlSession session = new FileCrawlSession(sessionName, this,
                crawlDefinition, crawlStatusBuilder, crawlCollector == null ? doNothing : crawlCollector);
        return new FileCrawlThread(this, session, LOGGER);
    }

    static final CrawlCollector<FileCrawlItem> doNothing = new CrawlCollector<>() {
        @Override
        public void collect(FileCrawlItem crawlItem) {
        }

        @Override
        public void done() {
        }
    };


}

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
package com.qwazr.crawler.ftp;

import com.qwazr.cluster.ClusterManager;
import com.qwazr.crawler.common.CrawlManager;
import com.qwazr.crawler.file.FileCrawlerManager;
import com.qwazr.utils.LoggerUtils;
import com.qwazr.utils.TimeTracker;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.logging.Logger;

public class FtpCrawlerManager extends CrawlManager
        <FtpCrawlerManager, FtpCrawlThread, FtpCrawlSession, FtpCrawlDefinition,
                FtpCrawlStatus, FtpCrawlItem> {

    private static final Logger LOGGER = LoggerUtils.getLogger(FileCrawlerManager.class);
    private final FtpCrawlerServiceInterface service;

    public FtpCrawlerManager(final Path crawlerRootDirectory,
                             final String myAddress,
                             final ExecutorService executorService) throws IOException {
        super(crawlerRootDirectory, myAddress, executorService,
                LOGGER, FtpCrawlStatus.class);
        service = new FtpCrawlerServiceImpl(this);
    }

    public FtpCrawlerManager(final Path crawlerRootDirectory,
                             final ClusterManager clusterManager,
                             final ExecutorService executorService) throws IOException {
        this(crawlerRootDirectory, clusterManager.getService().getStatus().me, executorService);
    }

    public FtpCrawlerManager(final Path crawlerRootDirectory,
                             final ExecutorService executorService) throws IOException {
        this(crawlerRootDirectory, (String) null, executorService);
    }

    public FtpCrawlerServiceInterface getService() {
        return service;
    }

    @Override
    protected FtpCrawlThread newCrawlThread(String sessionName, FtpCrawlDefinition crawlDefinition) {
        final TimeTracker timeTracker = TimeTracker.withDurations();
        final FtpCrawlStatus.Builder crawlStatusBuilder = FtpCrawlStatus.of(myAddress, timeTracker);
        final FtpCrawlCollectorFactory collectorFactory = newCrawlCollectorFactory(crawlDefinition, FtpCrawlCollectorFactory.class);
        final FtpCrawlSession session = new FtpCrawlSession(sessionName, this, timeTracker, crawlDefinition, attributes, crawlStatusBuilder, collectorFactory);
        return new FtpCrawlThread(this, session, LOGGER);
    }
}

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

import com.qwazr.crawler.common.CrawlCollector;
import com.qwazr.crawler.common.CrawlSessionBase;
import com.qwazr.utils.TimeTracker;

public class FtpCrawlSession extends CrawlSessionBase
        <FtpCrawlSession, FtpCrawlThread, FtpCrawlerManager, FtpCrawlDefinition, FtpCrawlSessionStatus, FtpCrawlItem> {

    FtpCrawlSession(final String sessionName,
                    final FtpCrawlerManager ftpCrawlerManager,
                    final TimeTracker timeTracker,
                    final FtpCrawlDefinition crawlDefinition,
                    final FtpCrawlSessionStatus.Builder crawlStatusBuilder,
                    final CrawlCollector<FtpCrawlItem> crawlCollector) {
        super(sessionName, ftpCrawlerManager, timeTracker, crawlDefinition, crawlStatusBuilder, crawlCollector);
    }
}

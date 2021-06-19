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

import com.qwazr.crawler.common.CrawlCollector;
import com.qwazr.crawler.common.CrawlSessionBase;

public class FileCrawlSession extends CrawlSessionBase
        <FileCrawlSession, FileCrawlThread, FileCrawlerManager, FileCrawlDefinition, FileCrawlSessionStatus, FileCrawlItem> {

    FileCrawlSession(final String sessionName,
                     final FileCrawlerManager fileCrawlerManager,
                     final FileCrawlDefinition crawlDefinition,
                     final FileCrawlSessionStatus.Builder crawlStatusBuilder,
                     final CrawlCollector<FileCrawlItem> fileCrawlCollector) {
        super(sessionName, fileCrawlerManager, crawlDefinition, crawlStatusBuilder, fileCrawlCollector);
    }

}

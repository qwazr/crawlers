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
package com.qwazr.crawler.file;

import com.qwazr.crawler.common.CrawlSessionBase;
import com.qwazr.utils.TimeTracker;
import java.util.Map;

public class FileCrawlSession extends CrawlSessionBase
        <FileCrawlSession, FileCrawlThread, FileCrawlerManager, FileCrawlDefinition, FileCrawlSessionStatus, FileCrawlItem> {

    FileCrawlSession(final String sessionName,
                     final FileCrawlerManager fileCrawlerManager,
                     final TimeTracker timeTracker,
                     final FileCrawlDefinition crawlDefinition,
                     final Map<String, Object> attributes,
                     final FileCrawlSessionStatus.Builder crawlStatusBuilder,
                     final FileCrawlCollectorFactory fileCollectorFactory) {
        super(sessionName, fileCrawlerManager, timeTracker, crawlDefinition, attributes, crawlStatusBuilder, fileCollectorFactory);
    }


}

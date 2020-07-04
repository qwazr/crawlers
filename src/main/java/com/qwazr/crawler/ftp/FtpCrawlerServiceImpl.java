/*
 * Copyright 2015-2020 Emmanuel Keller / QWAZR
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

import com.qwazr.crawler.common.CrawlerServiceImpl;
import com.qwazr.utils.LoggerUtils;

import java.io.IOException;
import java.util.logging.Logger;

class FtpCrawlerServiceImpl extends CrawlerServiceImpl
        <FtpCrawlSession, FtpCrawlThread, FtpCrawlerManager, FtpCrawlDefinition,
                FtpCrawlStatus, FtpCrawlStatus.Builder>
        implements FtpCrawlerServiceInterface {

    private static final Logger LOGGER = LoggerUtils.getLogger(FtpCrawlerServiceImpl.class);

    FtpCrawlerServiceImpl(FtpCrawlerManager crawlerManager) {
        super(LOGGER, crawlerManager);
    }

    public FtpCrawlStatus runSession(final String sessionName, final String jsonCrawlDefinition) throws IOException {
        return runSession(sessionName, FtpCrawlDefinition.newInstance(jsonCrawlDefinition));
    }

}

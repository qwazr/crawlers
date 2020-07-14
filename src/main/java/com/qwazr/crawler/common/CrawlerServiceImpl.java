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
package com.qwazr.crawler.common;

import com.qwazr.server.AbstractServiceImpl;
import com.qwazr.server.ServerException;

import javax.ws.rs.core.Response.Status;
import java.util.TreeMap;
import java.util.logging.Logger;

public abstract class CrawlerServiceImpl<
        SESSION extends CrawlSessionBase<SESSION, THREAD, MANAGER, DEFINITION, STATUS, ITEM>,
        THREAD extends CrawlThread<THREAD, DEFINITION, STATUS, MANAGER, SESSION, ITEM>,
        MANAGER extends CrawlManager<MANAGER, THREAD, SESSION, DEFINITION, STATUS, ITEM>,
        DEFINITION extends CrawlDefinition<DEFINITION>,
        STATUS extends CrawlStatus<STATUS>,
        ITEM extends CrawlItem<?>
        > extends AbstractServiceImpl implements CrawlerServiceInterface<DEFINITION, STATUS> {

    protected final Logger logger;

    protected final MANAGER crawlManager;

    protected CrawlerServiceImpl(Logger logger, MANAGER crawlManager) {
        this.logger = logger;
        this.crawlManager = crawlManager;
    }

    @Override
    public TreeMap<String, STATUS> getSessions() {
        final TreeMap<String, STATUS> map = new TreeMap<>();
        crawlManager.forEachLiveSession(map::put);
        return map;
    }

    public STATUS getSession(final String sessionName) {
        try {
            final STATUS status = crawlManager.getSessionStatus(sessionName);
            if (status != null)
                return status;
            throw new ServerException(Status.NOT_FOUND, "Session not found");
        } catch (Exception e) {
            throw ServerException.getJsonException(logger, e);
        }
    }

    @Override
    public boolean abortSession(final String sessionName, final String reason) {
        try {
            crawlManager.abortSession(sessionName, reason);
            return true;
        } catch (Exception e) {
            throw ServerException.getTextException(logger, e);
        }
    }

    public STATUS runSession(final String sessionName, final DEFINITION crawlDefinition) {
        try {
            return crawlManager.runSession(sessionName, crawlDefinition);
        } catch (Exception e) {
            throw ServerException.getJsonException(logger, e);
        }
    }

}

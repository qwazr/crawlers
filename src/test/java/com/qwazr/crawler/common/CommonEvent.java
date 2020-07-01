/*
 * Copyright 2017-2018 Emmanuel Keller / QWAZR
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

import com.qwazr.server.client.ErrorWrapper;
import com.qwazr.utils.WaitFor;
import org.junit.Assert;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.logging.Logger;

public class CommonEvent {

    public static CrawlStatus crawlWait(final String sessionName, final CrawlerServiceInterface service)
            throws InterruptedException {
        final AtomicReference<CrawlStatus> statusRef = new AtomicReference<>();
        WaitFor.of().timeOut(TimeUnit.MINUTES, 2).until(() -> {
            final CrawlStatus status = ErrorWrapper.bypass(() -> service.getSession(sessionName), 404);
            statusRef.set(status);
            return status != null && status.endTime != null;
        });
        return statusRef.get();
    }

    private final static Logger LOGGER = Logger.getLogger(CommonEvent.class.getName());

    public static class SessionCollector<D extends CrawlDefinition<D>, S extends CrawlSession<D, ?>, C extends CurrentCrawl> {

        protected final EventEnum eventEnum;
        protected final Map<EventEnum, Feedback<D, C>> feedbacks;

        public SessionCollector(final EventEnum eventEnum, final Map<EventEnum, Feedback<D, C>> feedbacks) {
            this.eventEnum = eventEnum;
            this.feedbacks = feedbacks;
        }

        public void collect(final S crawlSession, final C currentCrawl, final Map<String, ?> attributes) {
            feedbacks.computeIfAbsent(eventEnum, f -> new Feedback<>()).attributes(attributes);
            Assert.assertNotNull(crawlSession);
            LOGGER.info(eventEnum.name());
        }
    }

    public static class CrawlCollector<D extends CrawlDefinition<D>, S extends CrawlSession<D, ?>, C extends CurrentCrawl>
            extends SessionCollector<D, S, C> {

        private final Function<C, String> currentIdProvider;

        public CrawlCollector(final EventEnum eventEnum, final Map<EventEnum, Feedback<D, C>> feedbacks,
                              final Function<C, String> currentIdProvider) {
            super(eventEnum, feedbacks);
            this.currentIdProvider = currentIdProvider;
        }

        @Override
        public void collect(final S crawlSession, final C currentCrawl, final Map<String, ?> attributes) {
            super.collect(crawlSession, currentCrawl, attributes);
            feedbacks.computeIfAbsent(eventEnum, f -> new Feedback<>())
                    .current(currentCrawl, currentIdProvider.apply(currentCrawl));
            Assert.assertNotNull(currentCrawl);
            Assert.assertEquals(null, currentCrawl.getError());
        }

    }

    public static class Feedback<D extends CrawlDefinition, C extends CurrentCrawl> {

        public final AtomicInteger counters;
        public final Map<String, Object> variables;
        public final Map<String, C> currentCrawls;

        Feedback() {
            counters = new AtomicInteger();
            variables = new HashMap<>();
            currentCrawls = new HashMap<>();
        }

        void attributes(Map<String, ?> attributes) {
            counters.incrementAndGet();
            if (attributes != null)
                this.variables.putAll(attributes);
        }

        void current(C currentCrawl, String currentId) {
            if (currentCrawl != null)
                currentCrawls.put(currentId, currentCrawl);
        }

        public int count() {
            return counters.get();
        }

        public Object variable(String key) {
            return variables.get(key);
        }

        public Integer crawlDepth(String id) {
            return Objects.requireNonNull(currentCrawls.get(id), "Crawl not found: " + id).getDepth();
        }
    }

}

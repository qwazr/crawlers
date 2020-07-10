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
package com.qwazr.crawler.web;

import com.qwazr.crawler.CrawlerServer;
import com.qwazr.server.RemoteService;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeAll;

public class WebCrawlerTests {

    private final static String REMOTE_URL = "http://localhost:9091";

    public static class Local extends WebCrawlerTestAbstract {

        @BeforeAll
        public static void setup() throws Exception {
            WebCrawlerTestAbstract.setup();
            service = CrawlerServer.getInstance().getWebCrawlerServiceBuilder().local();
        }
    }

    public static class Remote extends WebCrawlerTestAbstract {

        @BeforeAll
        public static void setup() throws Exception {
            WebCrawlerTestAbstract.setup();
            service = CrawlerServer.getInstance()
                    .getWebCrawlerServiceBuilder()
                    .remote(RemoteService.of(REMOTE_URL).build());
            Assert.assertNotNull(service);
            Assert.assertEquals(WebCrawlerSingleClient.class, service.getClass());
        }
    }

    public static class Remotes extends WebCrawlerTestAbstract {

        @BeforeAll
        public static void setup() throws Exception {
            WebCrawlerTestAbstract.setup();
            service = CrawlerServer.getInstance()
                    .getWebCrawlerServiceBuilder()
                    .remotes(RemoteService.of(REMOTE_URL).build());
            Assert.assertNotNull(service);
            Assert.assertEquals(WebCrawlerMultiClient.class, service.getClass());
        }
    }

}

/**
 * Copyright 2016 Emmanuel Keller / QWAZR
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
 **/
package com.qwazr.crawler.web.test;

import com.qwazr.crawler.web.WebCrawlStatus;
import com.qwazr.crawler.web.WebCrawlerServer;
import com.qwazr.crawler.web.WebCrawlerServiceBuilder;
import com.qwazr.crawler.web.WebCrawlerServiceInterface;
import com.qwazr.server.RemoteService;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.TreeMap;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class WebCrawlerTest {

	private static WebCrawlerServiceInterface local;
	private static WebCrawlerServiceInterface remote;

	@Test
	public void test100startServer() throws Exception {
		WebCrawlerServer.main();
		local = WebCrawlerServer.getInstance().getServiceBuilder().local();
		Assert.assertNotNull(local);
		remote = new WebCrawlerServiceBuilder(null, null).remote(RemoteService.of("http://localhost:9091").build());
		Assert.assertNotNull(remote);
	}

	@Test
	public void test200emptySessions() {
		TreeMap<String, WebCrawlStatus> sessions = remote.getSessions(null);
		Assert.assertNotNull(sessions);
		Assert.assertTrue(sessions.isEmpty());
	}

	@Test
	public void test900stopServer() throws Exception {
		WebCrawlerServer.shutdown();
		Assert.assertNull(WebCrawlerServer.getInstance());
	}

}

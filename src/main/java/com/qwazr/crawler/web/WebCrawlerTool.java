/*
 * Copyright 2015-2017 Emmanuel Keller / QWAZR
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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.qwazr.crawler.web.driver.BrowserDriver;
import com.qwazr.crawler.web.driver.BrowserDriverBuilder;
import com.qwazr.library.AbstractLibrary;
import com.qwazr.utils.IOUtils;
import com.qwazr.utils.ObjectMappers;

import java.io.IOException;

@JsonIgnoreProperties(ignoreUnknown = true)
public class WebCrawlerTool extends AbstractLibrary {

	final public WebCrawlDefinition config = null;

	@JsonIgnore
	public WebCrawlDefinition.Builder getNewWebCrawlDefinition() {
		return config == null ? WebCrawlDefinition.of() : WebCrawlDefinition.of(config);
	}

	@JsonIgnore
	public BrowserDriver getNewWebDriver(final IOUtils.CloseableContext context, final String json)
			throws ReflectiveOperationException, SecurityException, IOException {
		final WebCrawlDefinition webCrawlDef = ObjectMappers.JSON.readValue(json, WebCrawlDefinition.class);
		final BrowserDriver browserDriver = new BrowserDriverBuilder(webCrawlDef).build();
		return context == null ? browserDriver : context.add(browserDriver);
	}

	@JsonIgnore
	public BrowserDriver getNewWebDriver(final IOUtils.CloseableContext context) throws ReflectiveOperationException {
		final BrowserDriver browserDriver = new BrowserDriverBuilder(config).build();
		return context == null ? browserDriver : context.add(browserDriver);
	}
}
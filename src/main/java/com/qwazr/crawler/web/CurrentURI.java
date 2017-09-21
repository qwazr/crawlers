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

import com.qwazr.crawler.common.CurrentCrawl;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public interface CurrentURI extends CurrentCrawl {

	/**
	 * The initial URI and the final URI may be different if any redirection was followed
	 *
	 * @return the current URI
	 */
	URI getUri();

	/**
	 * @return true if the Robots.txt disallow the current URI
	 */
	Boolean isRobotsTxtDisallow();

	/**
	 * Check if the URL has been redirected
	 *
	 * @return the redirect URL
	 */
	URI getRedirect();

	/**
	 * Get next level links
	 *
	 * @return a collection of links
	 */
	Map<URI, AtomicInteger> getLinks();

}

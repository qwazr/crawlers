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
package com.qwazr.crawler.web;

import com.qwazr.crawler.common.CrawlItem;
import com.qwazr.crawler.web.driver.DriverInterface;
import java.net.URI;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public interface WebCrawlItem extends CrawlItem<URI> {

    /**
     * Check if the URL has been redirected
     *
     * @return the redirect URL
     */
    URI getRedirect();

    /**
     * The HTTP status code of the crawled URI
     *
     * @return a status code
     */
    Integer getStatusCode();

    /**
     * The content-type of the crawled URI
     *
     * @return a content-type
     */
    String getContentType();

    /**
     * Get next level links
     *
     * @return a collection of links
     */
    Map<URI, AtomicInteger> getLinks();

    /**
     * Get the links who match the inclusion and exclusion patterns
     *
     * @return a filtered collection of links
     */
    Set<URI> getFilteredLinks();

    /**
     * @return the crawled body
     */
    DriverInterface.Body getBody();

}

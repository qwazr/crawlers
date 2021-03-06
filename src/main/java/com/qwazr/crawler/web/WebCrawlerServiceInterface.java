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

import com.qwazr.crawler.common.CrawlerServiceInterface;
import java.util.LinkedHashMap;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Path;
import javax.ws.rs.core.GenericType;

@RolesAllowed(WebCrawlerServiceInterface.SERVICE_NAME)
@Path(WebCrawlerServiceInterface.SERVICE_PATH)
public interface WebCrawlerServiceInterface extends CrawlerServiceInterface<WebCrawlDefinition, WebCrawlSessionStatus> {

    String SERVICE_NAME = "webcrawler";
    String SERVICE_PATH = "/crawler/web";

    GenericType<LinkedHashMap<String, WebCrawlSessionStatus>> mapStringCrawlType = new GenericType<>() {
    };

}

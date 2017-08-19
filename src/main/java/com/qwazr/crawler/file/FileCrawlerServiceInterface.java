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
package com.qwazr.crawler.file;

import com.fasterxml.jackson.core.type.TypeReference;
import com.qwazr.crawler.common.CrawlerServiceInterface;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Path;
import java.util.TreeMap;

@RolesAllowed(FileCrawlerServiceInterface.SERVICE_NAME)
@Path("/crawler/file")
public interface FileCrawlerServiceInterface extends CrawlerServiceInterface<FileCrawlDefinition, FileCrawlStatus> {

	String SERVICE_NAME = "filecrawler";

	TypeReference<TreeMap<String, FileCrawlStatus>> TreeMapStringCrawlTypeRef =
			new TypeReference<TreeMap<String, FileCrawlStatus>>() {
			};
}

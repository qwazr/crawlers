/*
 * Copyright 2017 Emmanuel Keller / QWAZR
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

import com.qwazr.utils.IOUtils;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

public class WebCrawlerDefinitionTest {

    @Test
    public void webCrawlDefinitionTest() throws IOException {
        final WebCrawlDefinition webCrawlDefJson = WebCrawlDefinition.newInstance(
                IOUtils.toString(WebCrawlerDefinitionTest.class.getResourceAsStream("web_crawl.json"),
                        StandardCharsets.UTF_8));
        Assert.assertNotNull(webCrawlDefJson);
        final WebCrawlDefinition webCrawlDef = WebCrawlDefinition.of(webCrawlDefJson).build();
        // Check that they are not the same reference
        Assert.assertTrue(webCrawlDef != webCrawlDefJson);
        // Check that they are equals
        Assert.assertEquals(webCrawlDef, webCrawlDefJson);
    }
}

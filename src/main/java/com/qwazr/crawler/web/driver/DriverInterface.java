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
package com.qwazr.crawler.web.driver;

import com.qwazr.crawler.web.WebCrawlDefinition;
import com.qwazr.crawler.web.WebRequestDefinition;
import org.jsoup.nodes.Document;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;

public interface DriverInterface extends Closeable {

    Head head(WebRequestDefinition request) throws IOException;

    Body body(WebRequestDefinition request) throws IOException;

    interface Head {

        String getUrl();

        int getResponseCode();

        Long getContentLength();

        String getContentType();

        String getContentEncoding();

        List<String> getHeaders(String name);

        String getFirstHeader(String name);

        String getRedirectLocation();

        boolean isSuccessful();
    }

    interface Body extends Head, Closeable {

        Content getContent();

        Document getHtmlDocument() throws IOException;
    }

    interface Content {

        InputStream getInput() throws IOException;

        String getContentType();

        Charset getCharset();

        String getCharsetName();

        Long getContentLength();

        boolean isClosed();
    }

    static DriverInterface of(WebCrawlDefinition webCrawlDef) {
        return new QwazrDriver(webCrawlDef);
    }

}
/*
 * Copyright 2014-2020 Emmanuel Keller / QWAZR
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

import com.qwazr.crawler.common.CrawlItemBase;
import com.qwazr.crawler.web.driver.DriverInterface;
import java.net.URI;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

final class WebCrawlItemImpl extends CrawlItemBase<URI> implements WebCrawlItem {

    private final URI redirect;
    private final Map<URI, AtomicInteger> links;
    private final Set<URI> filteredLinks;
    private final Boolean isRobotsTxtDisallow;
    private final Integer statusCode;
    private final String contentType;
    private final Boolean rejectedContentType;
    private final DriverInterface.Body body;

    WebCrawlItemImpl(final Builder builder) {
        super(builder);
        this.redirect = builder.redirect;
        this.isRobotsTxtDisallow = builder.isRobotsTxtDisallow;
        this.statusCode = builder.statusCode;
        this.contentType = builder.contentType;
        this.rejectedContentType = builder.rejectedContentType;
        this.links = builder.links == null ? Collections.emptyMap() : Collections.unmodifiableMap(builder.links);
        this.filteredLinks = builder.filteredLinks == null ?
                Collections.emptySet() :
                Collections.unmodifiableSet(builder.filteredLinks);
        this.body = builder.body;
    }

    @Override
    public URI getRedirect() {
        return redirect;
    }

    @Override
    public Integer getStatusCode() {
        return statusCode;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public Boolean isRejectedContentType() {
        return rejectedContentType;
    }

    @Override
    public Boolean isRobotsTxtDisallow() {
        return isRobotsTxtDisallow;
    }

    @Override
    public Map<URI, AtomicInteger> getLinks() {
        return links;
    }

    @Override
    public Set<URI> getFilteredLinks() {
        return filteredLinks;
    }

    @Override
    public DriverInterface.Body getBody() {
        return body;
    }

    final static class Builder extends BaseBuilder<URI, Builder> {

        final String uriString;
        private URI redirect;
        private Boolean isRobotsTxtDisallow;
        private Integer statusCode;
        private String contentType;
        private Boolean rejectedContentType;
        private LinkedHashMap<URI, AtomicInteger> links;
        private LinkedHashSet<URI> filteredLinks;
        private DriverInterface.Body body;

        protected Builder(URI uri, int depth) {
            super(uri, depth);
            this.uriString = uri.toASCIIString();
        }

        @Override
        protected Builder me() {
            return this;
        }

        public Builder redirect(URI redirect) {
            this.redirect = redirect == null ? null : item.resolve(redirect);
            return this;
        }

        public Builder robotsTxtDisallow(Boolean isRobotsTxtDisallow) {
            this.isRobotsTxtDisallow = isRobotsTxtDisallow;
            return this;
        }

        public Builder statusCode(Integer statusCode) {
            this.statusCode = statusCode;
            return this;
        }

        public Builder contentType(String contentType, boolean rejected) {
            this.contentType = contentType;
            this.rejectedContentType = rejected;
            return this;
        }

        public Builder link(URI uri) {
            if (uri == null)
                return this;
            if (links == null)
                links = new LinkedHashMap<>();
            links.computeIfAbsent(uri, u -> new AtomicInteger()).incrementAndGet();
            return this;
        }

        public Builder filteredLink(URI uri) {
            if (uri == null)
                return this;
            if (filteredLinks == null)
                filteredLinks = new LinkedHashSet<>();
            filteredLinks.add(uri);
            return this;
        }

        public Builder body(DriverInterface.Body body) {
            this.body = body;
            return this;
        }

        WebCrawlItem build() {
            return new WebCrawlItemImpl(this);
        }

    }
}

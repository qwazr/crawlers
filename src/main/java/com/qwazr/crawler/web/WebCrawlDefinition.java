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

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.qwazr.crawler.common.CrawlDefinition;
import com.qwazr.crawler.common.WildcardFilter;
import com.qwazr.utils.CollectionsUtils;
import com.qwazr.utils.ObjectMappers;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@JsonInclude(Include.NON_EMPTY)
@JsonAutoDetect(setterVisibility = JsonAutoDetect.Visibility.NONE,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        creatorVisibility = JsonAutoDetect.Visibility.NONE,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE,
        fieldVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY)
public class WebCrawlDefinition extends CrawlDefinition<WebCrawlDefinition> {

    /**
     * The entry point URL of the crawl.
     */
    @JsonProperty("entry_url")
    final public String entryUrl;

    /**
     * The entry point HTTP Method.
     */
    @JsonProperty("entry_request")
    final public WebRequestDefinition entryRequest;

    /**
     * A map of URL to crawl with the depth
     */
    @JsonProperty("urls")
    final public Map<String, Integer> urls;

    /**
     * The maximum number of URLs
     */
    @JsonProperty("max_url_number")
    final public Integer maxUrlNumber;

    @JsonProperty("accepted_content_type")
    final public List<String> acceptedContentType;

    /**
     * A list of regular expression patterns. Any parameters which matches is
     * removed.
     */
    @JsonProperty("parameters_patterns")
    final public List<String> parametersPatterns;

    /**
     * A list of regular expression patterns. Any matching group is remove from the path.
     */
    @JsonProperty("path_cleaner_patterns")
    final public List<String> pathCleanerPatterns;

    /**
     * Remove fragments from detected links
     */
    @JsonProperty("remove_fragments")
    final public Boolean removeFragments;

    /**
     * Cookies
     */
    @JsonProperty("cookie")
    final public Map<String, String> cookies;

    /**
     * The proxy definition(s)
     */
    @JsonProperty("proxies")
    final public List<ProxyDefinition> proxies;

    /**
     * Support of robots.txt protocol
     */
    @JsonProperty("robots_txt_enabled")
    final public Boolean robotsTxtEnabled;

    /**
     * Set the user agent to use for HTTP connection
     */
    @JsonProperty("user_agent")
    final public String userAgent;

    /**
     * Disable the SSL checks
     */
    @JsonProperty("disable_ssl_check")
    final public Boolean disableSslCheck;

    /**
     * Set the time out for HTTP connections
     */
    @JsonProperty("time_out_sec")
    final public Integer timeOutSecs;

    @JsonCreator
    protected WebCrawlDefinition(final @JsonProperty("max_depth") @JsonAlias("maxDepth") Integer maxDepth,
                                 final @JsonProperty("filters") List<Filter> filters,
                                 final @JsonProperty("filter_policy") @JsonAlias("filterPolicy") WildcardFilter.Status filterPolicy,
                                 final @JsonProperty("crawl_wait_ms") @JsonAlias("crawlWaitMs") Integer crawlWaitMs,
                                 final @JsonProperty("crawl_collector_factory") @JsonAlias("crawlCollectorFactory") String crawlCollectorFactoryClass,
                                 final @JsonProperty("variables") List<Variable> variables,
                                 final @JsonProperty("entry_url") @JsonAlias("entryUrl") String entryUrl,
                                 final @JsonProperty("entry_request") @JsonAlias("entryRequest") WebRequestDefinition entryRequest,
                                 final @JsonProperty("urls") Map<String, Integer> urls,
                                 final @JsonProperty("max_url_number") @JsonAlias("maxUrlNumber") Integer maxUrlNumber,
                                 final @JsonProperty("accepted_content_type") @JsonAlias("acceptedContentType") List<String> acceptedContentType,
                                 final @JsonProperty("parameters_patterns") @JsonAlias("parametersPatterns") List<String> parametersPatterns,
                                 final @JsonProperty("path_cleaner_patterns") @JsonAlias("pathCleanerPatterns") List<String> pathCleanerPatterns,
                                 final @JsonProperty("remove_fragments") @JsonAlias("removeFragments") Boolean removeFragments,
                                 final @JsonProperty("cookie") Map<String, String> cookies,
                                 final @JsonProperty("proxies") List<ProxyDefinition> proxies,
                                 final @JsonProperty("robots_txt_enabled") @JsonAlias("robotsTxtEnabled") Boolean robotsTxtEnabled,
                                 final @JsonProperty("user_agent") @JsonAlias("userAgent") String userAgent,
                                 final @JsonProperty("disable_ssl_check") @JsonAlias("disableSslCheck") Boolean disableSslCheck,
                                 final @JsonProperty("time_out_sec") @JsonAlias("timeOutSec") Integer timeOutSecs) {
        super(WebCrawlDefinition.class, crawlCollectorFactoryClass, variables, filters, filterPolicy, maxDepth, crawlWaitMs);
        this.entryUrl = entryUrl;
        this.entryRequest = entryRequest;
        this.urls = urls;
        this.maxUrlNumber = maxUrlNumber;
        this.acceptedContentType = acceptedContentType;
        this.parametersPatterns = parametersPatterns;
        this.pathCleanerPatterns = pathCleanerPatterns;
        this.removeFragments = removeFragments;
        this.robotsTxtEnabled = robotsTxtEnabled;
        this.userAgent = userAgent;
        this.cookies = cookies;
        this.proxies = proxies;
        this.disableSslCheck = disableSslCheck;
        this.timeOutSecs = timeOutSecs;
    }

    protected WebCrawlDefinition(Builder builder) {
        super(WebCrawlDefinition.class, builder);
        entryUrl = builder.entryUrl;
        entryRequest = builder.entryRequest;
        urls = builder.urls == null ? null : Collections.unmodifiableMap(new LinkedHashMap<>(builder.urls));
        maxUrlNumber = builder.maxUrlNumber;
        acceptedContentType = builder.acceptedContentType == null ?
                null :
                List.copyOf(new ArrayList<>(builder.acceptedContentType));
        parametersPatterns = builder.parametersPatterns == null ?
                null :
                List.copyOf(new ArrayList<>(builder.parametersPatterns));
        pathCleanerPatterns = builder.pathCleanerPatterns == null ?
                null :
                List.copyOf(new ArrayList<>(builder.pathCleanerPatterns));
        removeFragments = builder.removeFragments;
        robotsTxtEnabled = builder.robotsTxtEnabled;
        userAgent = builder.userAgent;
        cookies = builder.cookies == null ? null : Map.copyOf(new LinkedHashMap<>(builder.cookies));
        proxies = builder.proxies == null ? null : List.copyOf(new ArrayList<>(builder.proxies));
        disableSslCheck = builder.disableSslCheck;
        timeOutSecs = builder.timeOutSecs;
    }

    @JsonIgnore
    public String getEntryUrl() {
        return this.entryUrl;
    }

    @JsonIgnore
    public WebRequestDefinition getEntryRequest() {
        return entryRequest;
    }

    public Map<String, Integer> getUrls() {
        return this.urls;
    }

    @JsonIgnore
    public Integer getMaxUrlNumber() {
        return maxUrlNumber;
    }

    @JsonIgnore
    public Collection<String> getParametersPattern() {
        return parametersPatterns;
    }

    @JsonIgnore
    public Collection<String> getPathCleanerPatterns() {
        return pathCleanerPatterns;
    }

    @JsonIgnore
    public Boolean getRemoveFragments() {
        return removeFragments;
    }

    @JsonIgnore
    public Boolean getRobotsTxtEnabled() {
        return robotsTxtEnabled;
    }

    @JsonIgnore
    public String getUserAgent() {
        return userAgent;
    }

    public Collection<ProxyDefinition> getProxies() {
        return proxies;
    }

    public Map<String, String> getCookies() {
        return cookies;
    }

    @JsonIgnore
    public Boolean getDisableSslCheck() {
        return disableSslCheck;
    }

    @JsonIgnore
    public Integer getTimeOutSecs() {
        return timeOutSecs;
    }

    @Override
    protected int computeHashCode() {
        return Objects.hash(entryUrl, entryRequest, maxUrlNumber, super.computeHashCode());
    }

    @Override
    protected boolean isEqual(final WebCrawlDefinition w) {
        return super.isEqual(w) &&
                Objects.equals(entryUrl, w.entryUrl) &&
                Objects.equals(entryRequest, w.entryRequest) &&
                CollectionsUtils.equals(urls, w.urls) &&
                Objects.equals(maxUrlNumber, w.maxUrlNumber) &&
                CollectionsUtils.equals(acceptedContentType, w.acceptedContentType) &&
                CollectionsUtils.equals(parametersPatterns, w.parametersPatterns) &&
                CollectionsUtils.equals(pathCleanerPatterns, w.pathCleanerPatterns) &&
                Objects.equals(removeFragments, w.removeFragments) &&
                Objects.equals(robotsTxtEnabled, w.robotsTxtEnabled) &&
                Objects.equals(userAgent, w.userAgent) &&
                CollectionsUtils.equals(cookies, w.cookies) &&
                CollectionsUtils.equals(proxies, w.proxies) &&
                Objects.equals(disableSslCheck, w.disableSslCheck) &&
                Objects.equals(timeOutSecs, w.timeOutSecs);
    }

    public static WebCrawlDefinition newInstance(final String json) throws IOException {
        return ObjectMappers.JSON.readValue(json, WebCrawlDefinition.class);
    }

    public static Builder of() {
        return new Builder();
    }

    public static Builder of(final WebCrawlDefinition webCrawlDefinition) {
        return new Builder(webCrawlDefinition);
    }

    public static class Builder extends AbstractBuilder<WebCrawlDefinition, Builder> {

        private String entryUrl;
        private WebRequestDefinition entryRequest;
        private LinkedHashMap<String, Integer> urls;
        private Integer maxUrlNumber;
        private LinkedHashSet<String> acceptedContentType;
        private LinkedHashSet<String> parametersPatterns;
        private LinkedHashSet<String> pathCleanerPatterns;
        private Boolean removeFragments;
        private LinkedHashMap<String, String> cookies;
        private List<ProxyDefinition> proxies;
        private Boolean robotsTxtEnabled;
        private String userAgent;
        private Boolean disableSslCheck;
        private Integer timeOutSecs;

        protected Builder() {
            super();
        }

        protected Builder(WebCrawlDefinition src) {
            super(src);
            this.entryUrl = src.entryUrl;
            this.entryRequest = src.entryRequest;
            this.urls = src.urls == null ? null : new LinkedHashMap<>(src.urls);
            this.maxDepth = src.maxDepth;
            this.maxUrlNumber = src.maxUrlNumber;
            this.acceptedContentType =
                    src.acceptedContentType == null ? null : new LinkedHashSet<>(src.acceptedContentType);
            this.parametersPatterns =
                    src.parametersPatterns == null ? null : new LinkedHashSet<>(src.parametersPatterns);
            this.pathCleanerPatterns =
                    src.pathCleanerPatterns == null ? null : new LinkedHashSet<>(src.pathCleanerPatterns);
            this.removeFragments = src.removeFragments;
            this.cookies = src.cookies == null ? null : new LinkedHashMap<>(src.cookies);
            this.proxies = src.proxies == null ? null : new ArrayList<>(src.proxies);
            this.robotsTxtEnabled = src.robotsTxtEnabled;
            this.userAgent = src.userAgent;
            this.crawlWaitMs = src.crawlWaitMs;
            this.disableSslCheck = src.disableSslCheck;
            this.timeOutSecs = src.timeOutSecs;
        }

        public Builder setUrls(final LinkedHashMap<String, Integer> urls) {
            this.urls = urls;
            return this;
        }

        public Builder setEntryUrl(final String entryUrl) {
            this.entryUrl = entryUrl;
            return this;
        }

        public Builder setEntryRequest(final WebRequestDefinition request) {
            this.entryRequest = request;
            return this;
        }

        public Builder addUrl(final String url, final Integer depth) {
            if (urls == null)
                urls = new LinkedHashMap<>();
            urls.put(url, depth);
            return this;
        }

        public Builder setMaxUrlNumber(final Integer maxUrlNumber) {
            this.maxUrlNumber = maxUrlNumber;
            return this;
        }

        public Builder addAcceptedContentType(final String acceptedContentType) {
            if (this.acceptedContentType == null)
                this.acceptedContentType = new LinkedHashSet<>();
            this.acceptedContentType.add(acceptedContentType);
            return this;
        }

        public Builder addParametersPattern(final String parametersPattern) {
            if (this.parametersPatterns == null)
                this.parametersPatterns = new LinkedHashSet<>();
            this.parametersPatterns.add(parametersPattern);
            return this;
        }

        public Builder addPathCleanerPattern(final String pathCleanerPattern) {
            if (this.pathCleanerPatterns == null)
                this.pathCleanerPatterns = new LinkedHashSet<>();
            this.pathCleanerPatterns.add(pathCleanerPattern);
            return this;
        }

        public Builder setRemoveFragments(final Boolean removeFragments) {
            this.removeFragments = removeFragments;
            return this;
        }

        public Builder setRobotsTxtEnabled(final Boolean robotsTxtEnabled) {
            this.robotsTxtEnabled = robotsTxtEnabled;
            return this;
        }

        public Builder addProxy(final ProxyDefinition proxy) {
            if (proxy == null)
                return this;
            if (proxies == null)
                proxies = new ArrayList<>();
            proxies.add(proxy);
            return this;
        }

        public Builder addCookie(final String name, final String value) {
            if (name == null || value == null)
                return this;
            if (cookies == null)
                cookies = new LinkedHashMap<>();
            cookies.put(name, value);
            return this;
        }

        public Builder setCookies(final Map<String, String> cookies) {
            if (this.cookies == null)
                this.cookies = new LinkedHashMap<>();
            if (cookies != null)
                this.cookies.putAll(cookies);
            return this;
        }

        public Builder setDisableSslCheck(Boolean disableSslCheck) {
            this.disableSslCheck = disableSslCheck;
            return this;
        }

        public Builder setTimeOutSecs(Integer timeOutSecs) {
            this.timeOutSecs = timeOutSecs;
            return this;
        }

        public Builder userAgent(String userAgent) {
            this.userAgent = userAgent;
            return this;
        }

        @Override
        protected Builder me() {
            return this;
        }

        @Override
        public WebCrawlDefinition build() {
            return new WebCrawlDefinition(this);
        }
    }
}

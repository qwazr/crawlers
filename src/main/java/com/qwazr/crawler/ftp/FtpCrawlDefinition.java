/*
 * Copyright 2017-2020 Emmanuel Keller / QWAZR
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
package com.qwazr.crawler.ftp;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.qwazr.crawler.common.CrawlDefinition;
import com.qwazr.crawler.common.WildcardFilter;
import com.qwazr.utils.ObjectMappers;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

@JsonInclude(Include.NON_EMPTY)
@JsonAutoDetect(setterVisibility = JsonAutoDetect.Visibility.NONE,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        creatorVisibility = JsonAutoDetect.Visibility.NONE,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE,
        fieldVisibility = JsonAutoDetect.Visibility.PROTECTED_AND_PUBLIC)
public class FtpCrawlDefinition extends CrawlDefinition<FtpCrawlDefinition> {

    /**
     * The host name of the FTP server.
     */
    @JsonProperty("hostname")
    final public String hostname;

    /**
     * The port of the FTP server.
     */
    @JsonProperty("port")
    final public Integer port;

    /**
     * The entry point PATH of the crawl.
     */
    @JsonProperty("entry_path")
    final public String entryPath;

    /**
     * The username to use for the FTP connection
     */
    @JsonProperty("username")
    final public String username;

    /**
     * The password to use for the FTP connection
     */
    @JsonProperty("password")
    final public String password;

    /**
     * Enable FTP over SSL
     */
    @JsonProperty("is_ssl")
    final public Boolean isSsl;

    /**
     * Set the data connection mode to passive
     */
    @JsonProperty("is_passive")
    final public Boolean isPassive;

    @JsonCreator
    protected FtpCrawlDefinition(final @JsonProperty("max_depth") @JsonAlias("maxDepth") Integer maxDepth,
                                 final @JsonProperty("filters") List<Filter> filters,
                                 final @JsonProperty("filter_policy") @JsonAlias("filterPolicy") WildcardFilter.Status filterPolicy,
                                 final @JsonProperty("crawl_wait_ms") @JsonAlias("crawlWaitMs") Integer crawlWaitMs,
                                 final @JsonProperty("crawl_collector_factory") @JsonAlias("crawlCollectorFactory") String crawlCollectorFactoryClass,
                                 final @JsonProperty("variables") List<Variable> variables,
                                 final @JsonProperty("hostname") String hostname,
                                 final @JsonProperty("port") Integer port,
                                 final @JsonProperty("entry_path") @JsonAlias("entryPath") String entryPath,
                                 final @JsonProperty("username") String username,
                                 final @JsonProperty("password") String password,
                                 final @JsonProperty("is_ssl") @JsonAlias("isSsl") Boolean isSsl,
                                 final @JsonProperty("is_passive") @JsonAlias("isPassive") Boolean isPassive) {
        super(FtpCrawlDefinition.class, crawlCollectorFactoryClass, variables, filters, filterPolicy, maxDepth, crawlWaitMs);
        this.hostname = hostname;
        this.port = port;
        this.entryPath = entryPath;
        this.username = username;
        this.password = password;
        this.isSsl = isSsl;
        this.isPassive = isPassive;
    }

    private FtpCrawlDefinition(Builder builder) {
        super(FtpCrawlDefinition.class, builder);
        this.hostname = builder.hostname;
        this.port = builder.port;
        this.entryPath = builder.entryPath;
        this.username = builder.username;
        this.password = builder.password;
        this.isSsl = builder.isSsl;
        this.isPassive = builder.isPassive;
    }

    @Override
    protected int computeHashCode() {
        return Objects.hash(hostname, port, entryPath);
    }

    @Override
    protected boolean isEqual(final FtpCrawlDefinition f) {
        return super.isEqual(f) &&
                Objects.equals(hostname, f.hostname) &&
                Objects.equals(port, f.port) &&
                Objects.equals(entryPath, f.entryPath) &&
                Objects.equals(username, f.username) &&
                Objects.equals(password, f.password) &&
                Objects.equals(isSsl, f.isSsl) &&
                Objects.equals(isPassive, f.isPassive);
    }

    @JsonIgnore
    public String getHostName() {
        return this.hostname;
    }

    @JsonIgnore
    public Integer getPort() {
        return this.port;
    }

    @JsonIgnore
    public String getEntryPath() {
        return this.entryPath;
    }

    @JsonIgnore
    public String getUsername() {
        return username;
    }

    @JsonIgnore
    public String getPassword() {
        return password;
    }

    @JsonIgnore
    public Boolean isSsl() {
        return isSsl;
    }

    @JsonIgnore
    public Boolean isPassive() {
        return isPassive;
    }

    public static Builder of() {
        return new Builder();
    }

    public static Builder of(FtpCrawlDefinition crawlDefinition) {
        return new Builder(crawlDefinition);
    }

    public static class Builder extends AbstractBuilder<FtpCrawlDefinition, Builder> {

        private String hostname;

        private Integer port;

        private String entryPath;

        private String username;

        private String password;

        private Boolean isSsl;

        private Boolean isPassive;

        protected Builder() {
        }

        protected Builder(FtpCrawlDefinition crawlDefinition) {
            super(crawlDefinition);
            hostname = crawlDefinition.hostname;
            port = crawlDefinition.port;
            entryPath = crawlDefinition.entryPath;
            maxDepth = crawlDefinition.maxDepth;
            username = crawlDefinition.username;
            password = crawlDefinition.password;
            isSsl = crawlDefinition.isSsl;
        }

        public Builder hostname(final String hostname) {
            this.hostname = hostname;
            return this;
        }

        public Builder port(final Integer port) {
            this.port = port;
            return this;
        }

        public Builder username(final String username) {
            this.username = username;
            return this;
        }

        public Builder password(final String password) {
            this.password = password;
            return this;
        }

        public Builder ssl(final Boolean ssl) {
            this.isSsl = ssl;
            return this;
        }

        public Builder passive(final Boolean passive) {
            this.isPassive = passive;
            return this;
        }

        public Builder entryPath(final String entryPath) {
            this.entryPath = entryPath;
            return this;
        }

        @Override
        protected Builder me() {
            return this;
        }

        @Override
        public FtpCrawlDefinition build() {
            return new FtpCrawlDefinition(this);
        }
    }

    @JsonIgnore
    public static FtpCrawlDefinition newInstance(final String json) throws IOException {
        return ObjectMappers.JSON.readValue(json, FtpCrawlDefinition.class);
    }

}

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

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(setterVisibility = JsonAutoDetect.Visibility.NONE,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        creatorVisibility = JsonAutoDetect.Visibility.NONE,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE,
        fieldVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY)
public class WebRequestDefinition {

    public final String url;

    public final String charset;

    public final Map<String, String> headers;

    public final Map<String, List<String>> parameters;

    public final HttpMethod method;

    @JsonProperty("form_encoding_type")
    public final FormEncodingType formEncodingType;

    public enum FormEncodingType {
        URL_ENCODED, MULTIPART
    }

    public enum HttpMethod {
        HEAD, GET, POST, PUT, DELETE, PATCH
    }

    @JsonCreator
    public WebRequestDefinition(@JsonProperty("url") final String url,
                                @JsonProperty("charset") final String charset,
                                @JsonProperty("headers") final Map<String, String> headers,
                                @JsonProperty("parameters") Map<String, List<String>> parameters,
                                @JsonProperty("method") final HttpMethod method,
                                @JsonProperty("form_encoding_type") final FormEncodingType formEncodingType) {
        this.url = url;
        this.charset = charset;
        this.headers = headers;
        this.parameters = parameters;
        this.method = method;
        this.formEncodingType = formEncodingType;
    }

    private WebRequestDefinition(Builder builder) {
        this(builder.url, builder.charset,
                builder.headers == null ? null : Collections.unmodifiableMap(new LinkedHashMap<>(builder.headers)),
                builder.parameters == null ?
                        null :
                        Collections.unmodifiableMap(new LinkedHashMap<>(builder.parameters)), builder.method,
                builder.formEncodingType);
    }

    public static WebRequestDefinition.Builder of(String url) {
        return new Builder().url(url);
    }

    public static WebRequestDefinition.Builder of(WebRequestDefinition request) {
        return of(request.url).charset(request.charset)
                .headers(request.headers)
                .parameters(request.parameters)
                .httpMethod(request.method)
                .formEncodingType(request.formEncodingType);
    }

    public static class Builder {

        private String url;

        private String charset;

        private LinkedHashMap<String, String> headers;

        private LinkedHashMap<String, List<String>> parameters;

        private HttpMethod method;

        private FormEncodingType formEncodingType;

        Builder() {
            url = null;
            charset = null;
            headers = null;
            parameters = null;
            method = HttpMethod.GET;
            formEncodingType = FormEncodingType.URL_ENCODED;
        }

        public Builder url(final String url) {
            this.url = url;
            return this;
        }

        public Builder url(final URI uri) {
            this.url = uri == null ? null : uri.toString();
            return this;
        }

        public Builder charset(final String charset) {
            this.charset = charset;
            return this;
        }

        public Builder header(final String key, final String value) {
            if (headers == null)
                headers = new LinkedHashMap<>();
            headers.put(key, value);
            return this;
        }

        public Builder headers(final Map<String, String> headers) {
            if (headers != null)
                headers.forEach(this::header);
            return this;
        }

        public Builder parameter(final String key, final String value) {
            if (parameters == null)
                parameters = new LinkedHashMap<>();
            parameters.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
            return this;
        }

        public Builder parameters(Map<String, List<String>> parameters) {
            if (parameters != null)
                parameters.forEach((key, values) -> values.forEach(value -> parameter(key, value)));
            return this;
        }

        public Builder httpMethod(final HttpMethod method) {
            this.method = method;
            return this;
        }

        public Builder formEncodingType(final FormEncodingType formEncodingType) {
            this.formEncodingType = formEncodingType;
            return this;
        }

        public WebRequestDefinition build() {
            return new WebRequestDefinition(this);
        }

    }
}

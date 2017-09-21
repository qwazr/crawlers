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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
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
		GET, POST, PUT, DELETE, PATCH
	}

	@JsonCreator
	public WebRequestDefinition(@JsonProperty("url") final String url, @JsonProperty("charset") final String charset,
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
				builder.headers == null ? null : Collections.unmodifiableMap(builder.headers),
				builder.parameters == null ? null : Collections.unmodifiableMap(builder.parameters), builder.method,
				builder.formEncodingType);
	}

	public static class Builder {

		private String url;

		private String charset;

		private LinkedHashMap<String, String> headers;

		private LinkedHashMap<String, List<String>> parameters;

		private HttpMethod method;

		private FormEncodingType formEncodingType;

		public Builder(final String url) {
			setUrl(url);
			charset = null;
			headers = null;
			parameters = null;
			method = HttpMethod.GET;
			formEncodingType = FormEncodingType.URL_ENCODED;
		}

		public Builder setUrl(final String url) {
			this.url = url;
			return this;
		}

		public Builder setCharset(final String charset) {
			this.charset = charset;
			return this;
		}

		public Builder addHeader(final String key, final String value) {
			if (headers == null)
				headers = new LinkedHashMap<>();
			headers.put(key, value);
			return this;
		}

		public Builder addParameter(final String key, final String value) {
			if (parameters == null)
				parameters = new LinkedHashMap<>();
			parameters.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
			return this;
		}

		public Builder setHttpMethod(final HttpMethod method) {
			this.method = method;
			return this;
		}

		public Builder setFormEncodingType(final FormEncodingType formEncodingType) {
			this.formEncodingType = formEncodingType;
			return this;
		}

		public WebRequestDefinition build() {
			return new WebRequestDefinition(this);
		}
	}
}

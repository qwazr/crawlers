/**
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
 **/

package com.qwazr.crawler.web;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.gargoylesoftware.htmlunit.HttpMethod;

import java.util.ArrayList;
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

	public final FormEncodingType form_encoding_type;

	public enum FormEncodingType {
		URL_ENCODED, MULTIPART
	}

	public WebRequestDefinition() {
		url = null;
		charset = null;
		headers = null;
		parameters = null;
		method = null;
		form_encoding_type = null;
	}

	private WebRequestDefinition(Builder builder) {
		this.url = builder.url;
		this.charset = builder.charset;
		this.headers = builder.headers == null ? null : new LinkedHashMap<>(builder.headers);
		this.parameters = builder.parameters == null ? null : builder.parameters;
		this.method = builder.method;
		this.form_encoding_type = builder.form_encoding_type;
	}

	public static class Builder {

		private String url;

		private String charset;

		private Map<String, String> headers;

		private Map<String, List<String>> parameters;

		private HttpMethod method;

		private FormEncodingType form_encoding_type;

		public Builder(final String url) {
			setUrl(url);
			charset = null;
			headers = null;
			parameters = null;
			method = HttpMethod.GET;
			form_encoding_type = form_encoding_type.URL_ENCODED;
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
			List<String> values = parameters.get(key);
			if (values == null) {
				values = new ArrayList<>();
				parameters.put(key, values);
			}
			values.add(value);
			return this;
		}

		public Builder setHttpMethod(final HttpMethod method) {
			this.method = method;
			return this;
		}

		public Builder setFormEncodingType(final FormEncodingType formEncodingType) {
			this.form_encoding_type = formEncodingType;
			return this;
		}

		public WebRequestDefinition build() {
			return new WebRequestDefinition(this);
		}
	}
}

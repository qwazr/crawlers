/**
 * Copyright 2014-2016 Emmanuel Keller / QWAZR
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

package com.qwazr.crawler.web.service;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.util.NameValuePair;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class WebRequestDefinition {

	public final String url;

	public final String charset;

	public final Map<String, String> headers;

	public final Map<String, String> parameters;

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
		this.parameters = builder.parameters == null ? null : new LinkedHashMap<>(builder.parameters);
		this.method = builder.method;
		this.form_encoding_type = builder.form_encoding_type;
	}

	public WebRequest getNewWebRequest() throws MalformedURLException {

		WebRequest request = new WebRequest(new URL(url));

		// Set the HTTP method
		if (method != null)
			request.setHttpMethod(method);

		// Set the charset
		if (charset != null)
			request.setCharset(charset);

		// Set the request parameters
		if (parameters != null) {
			List<NameValuePair> requestParameters = new ArrayList<>(parameters.size());
			parameters.forEach((key, value) -> requestParameters.add(new NameValuePair(key, value)));
			request.setRequestParameters(requestParameters);
		}

		// Set the request headers
		if (headers != null)
			request.setAdditionalHeaders(headers);

		// Set the form type
		if (form_encoding_type != null) {
			switch (form_encoding_type) {
			case URL_ENCODED:
				request.setEncodingType(com.gargoylesoftware.htmlunit.FormEncodingType.URL_ENCODED);
				break;
			case MULTIPART:
				request.setEncodingType(com.gargoylesoftware.htmlunit.FormEncodingType.MULTIPART);
				break;
			}
		}

		return request;
	}

	public static class Builder {

		private String url;

		private String charset;

		private Map<String, String> headers;

		private Map<String, String> parameters;

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

		public Builder addHeader(final String key, String value) {
			if (headers == null)
				headers = new LinkedHashMap<>();
			headers.put(key, value);
			return this;
		}

		public Builder addParameter(final String key, String value) {
			if (parameters == null)
				parameters = new LinkedHashMap<>();
			parameters.put(key, value);
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

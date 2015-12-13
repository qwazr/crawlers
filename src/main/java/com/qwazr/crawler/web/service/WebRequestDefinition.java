/**
 * Copyright 2014-2015 Emmanuel Keller / QWAZR
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
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
			List<NameValuePair> requestParameters = new ArrayList<NameValuePair>(parameters.size());
			for (Map.Entry<String, String> entry : parameters.entrySet())
				requestParameters.add(new NameValuePair(entry.getKey(), entry.getValue()));
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
}

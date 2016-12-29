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
package com.qwazr.crawler.web.driver;

import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.util.NameValuePair;
import com.qwazr.crawler.web.WebRequestDefinition;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.htmlunit.HtmlUnitWebElement;

import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class HtmlUnitDriverWebClient extends HtmlUnitDriver {

	public HtmlUnitDriverWebClient(Capabilities capabilities) {
		super(capabilities);
		if (capabilities != null) {
			String language = (String) capabilities.getCapability(AdditionalCapabilities.QWAZR_BROWSER_LANGUAGE);
			if (language != null) {
				BrowserVersion version = getWebClient().getBrowserVersion();
				version.setBrowserLanguage(language);
				version.setSystemLanguage(language);
				version.setUserLanguage(language);
			}
		}

	}

	@Override
	protected WebClient modifyWebClient(WebClient webClient) {
		webClient = super.modifyWebClient(webClient);
		webClient.setCssErrorHandler(new SilentCssErrorHandler());
		webClient.getOptions().setUseInsecureSSL(true);
		webClient.setRefreshHandler(null);
		webClient.getOptions().setThrowExceptionOnScriptError(false);
		webClient.setAjaxController(new NicelyResynchronizingAjaxController());
		webClient.setHTMLParserListener(null);
		return webClient;
	}

	@Override
	public WebClient getWebClient() {
		return super.getWebClient();
	}

	WebElement convertNode(DomElement element) {
		return newHtmlUnitWebElement(element);
	}

	protected WebElement newHtmlUnitWebElement(DomElement element) {
		return new HtmlUnitDriverWebElement(this, element);
	}

	public class HtmlUnitDriverWebElement extends HtmlUnitWebElement {

		public HtmlUnitDriverWebElement(HtmlUnitDriver parent, DomElement element) {
			super(parent, element);
		}

		DomElement getDomElement() {
			return super.getElement();
		}
	}

	public void request(final WebRequestDefinition webRequestDef) {
		final WebRequest request;
		try {
			request = getNewWebRequest(webRequestDef);
		} catch (MalformedURLException e) {
			throw new WebDriverException(e);
		}
		final WebWindow window = getCurrentWindow().getTopWindow();
		try {
			final Page page = getWebClient().getPage(request);
			window.setEnclosedPage(page);
		} catch (UnknownHostException e) {
			window.setEnclosedPage(new UnexpectedPage(new StringWebResponse("Unknown host", request.getUrl()),
					getCurrentWindow().getTopWindow()));
		} catch (ConnectException e) {
			// This might be expected
		} catch (SocketTimeoutException e) {
			throw new TimeoutException(e);
		} catch (Exception e) {
			throw new WebDriverException(e);
		}
	}

	private WebRequest getNewWebRequest(WebRequestDefinition requestDef) throws MalformedURLException {

		WebRequest request = new WebRequest(new URL(requestDef.url));

		// Set the HTTP method
		if (requestDef.method != null)
			request.setHttpMethod(requestDef.method);

		// Set the charset
		if (requestDef.charset != null)
			request.setCharset(requestDef.charset);

		// Set the request parameters
		if (requestDef.parameters != null) {
			List<NameValuePair> requestParameters = new ArrayList<>(requestDef.parameters.size());
			requestDef.parameters.forEach(
					(key, values) -> values.forEach((value) -> requestParameters.add(new NameValuePair(key, value))));
			request.setRequestParameters(requestParameters);
		}

		// Set the request headers
		if (requestDef.headers != null)
			request.setAdditionalHeaders(requestDef.headers);

		// Set the form type
		if (requestDef.form_encoding_type != null) {
			switch (requestDef.form_encoding_type) {
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

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

import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebClientOptions;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.WebWindow;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.qwazr.crawler.web.WebRequestDefinition;
import com.qwazr.utils.IOUtils;
import com.qwazr.utils.LoggerUtils;
import com.qwazr.utils.ObjectMappers;
import com.qwazr.utils.http.HttpUtils;
import org.apache.http.entity.mime.MIME;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebElement;
import org.w3c.css.sac.CSSException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HtmlUnitBrowserDriver extends HtmlUnitDriverWebClient
		implements AdditionalCapabilities.ResponseHeader, AdditionalCapabilities.SafeText,
		AdditionalCapabilities.InnerHtml, AdditionalCapabilities.SaveBinaryFile, AdditionalCapabilities.WebRequest {

	protected static final Logger logger = LoggerUtils.getLogger(HtmlUnitBrowserDriver.class);

	public HtmlUnitBrowserDriver() {
	}

	public HtmlUnitBrowserDriver(Capabilities capabilities) {
		super(capabilities);
	}

	private Page getEnclosedPage() {
		WebClient webClient = getWebClient();
		if (webClient == null)
			return null;
		WebWindow webWindow = webClient.getCurrentWindow();
		if (webWindow == null)
			return null;
		return webWindow.getEnclosedPage();
	}

	public void test() {
		getWebClient().getCurrentWindow().getTopWindow();
	}

	private WebResponse getWebResponse() {
		Page page = getEnclosedPage();
		if (page == null)
			return null;
		return page.getWebResponse();
	}

	@Override
	public Integer getStatusCode() {
		WebResponse response = getWebResponse();
		if (response == null)
			return null;
		return response.getStatusCode();
	}

	@Override
	public String getContentType() {
		WebResponse response = getWebResponse();
		if (response == null)
			return null;
		return response.getContentType();
	}

	@Override
	public String getContentDisposition() {
		WebResponse response = getWebResponse();
		if (response == null)
			return null;
		return response.getResponseHeaderValue(MIME.CONTENT_DISPOSITION);
	}

	public String getContentDispositionFilename() {
		return HttpUtils.getHeaderParameter(getContentDisposition(), "filename");
	}

	@Override
	public String getInnerHtmlByXPath(String xPath) {
		Page page = getEnclosedPage();
		if (page == null)
			return null;
		if (!(page instanceof HtmlPage))
			return null;
		HtmlPage htmlPage = (HtmlPage) page;
		DomElement domElement = htmlPage.getFirstByXPath(xPath);
		if (domElement == null)
			return null;
		return domElement.asXml();
	}

	@Override
	public void saveBinaryFile(File file) throws IOException {
		WebResponse response = getWebResponse();
		if (response == null)
			return;
		InputStream inputStream = response.getContentAsStream();
		if (inputStream == null)
			return;
		try {
			FileOutputStream fos = new FileOutputStream(file);
			try {
				IOUtils.copy(inputStream, fos);
			} finally {
				IOUtils.close(fos);
			}
		} finally {
			IOUtils.close(inputStream);
		}
	}

	public void request(final String json) throws IOException {
		request(ObjectMappers.JSON.readValue(json, WebRequestDefinition.class));
	}

	@Override
	public String getTextSafe(WebElement webElement) {
		if (webElement == null)
			return null;
		WebClientOptions options = getWebClient().getOptions();
		boolean cssEnabled = options.isCssEnabled();
		try {
			try {
				return webElement.getText();
			} catch (CSSException e) {
				logger.log(Level.WARNING, "Temporary disabling CSS", e);
			}
			options.setCssEnabled(false);
			return webElement.getText();
		} finally {
			options.setCssEnabled(cssEnabled);
		}
	}

}

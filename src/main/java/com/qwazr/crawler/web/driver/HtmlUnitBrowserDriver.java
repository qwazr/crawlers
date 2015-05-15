/**
 * Copyright 2014-2015 Emmanuel Keller / QWAZR
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/
package com.qwazr.crawler.web.driver;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.openqa.selenium.Capabilities;

import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.WebWindow;

public class HtmlUnitBrowserDriver extends
		BrowserDriver<HtmlUnitDriverWebClient> {

	private List<InputStream> inputStreamList = null;

	public HtmlUnitBrowserDriver(Capabilities capabilities) {
		super(BrowserDriverEnum.html_unit, capabilities);
	}

	@Override
	public HtmlUnitDriverWebClient initialize(Capabilities capabilities) {
		if (capabilities == null)
			return new HtmlUnitDriverWebClient();
		else
			return new HtmlUnitDriverWebClient(capabilities);
	}

	@Override
	public void close() {
		if (inputStreamList != null) {
			for (InputStream inputStream : inputStreamList)
				IOUtils.closeQuietly(inputStream);
			inputStreamList.clear();
		}
	}

	public WebClient getWebClient() {
		return driver.getWebClient();
	}

	public WebResponse getWebResponse() {
		WebClient webClient = getWebClient();
		if (webClient == null)
			return null;
		WebWindow webWindow = webClient.getCurrentWindow();
		if (webWindow == null)
			return null;
		Page page = webWindow.getEnclosedPage();
		if (page == null)
			return null;
		return page.getWebResponse();
	}

	public Integer getStatusCode() {
		WebResponse response = getWebResponse();
		if (response == null)
			return null;
		return response.getStatusCode();
	}

	public String getContentType() {
		WebResponse response = getWebResponse();
		if (response == null)
			return null;
		return response.getContentType();
	}

	public InputStream getInputStream() throws IOException {
		WebResponse response = getWebResponse();
		if (response == null)
			return null;
		if (inputStreamList == null)
			inputStreamList = new ArrayList<InputStream>();
		InputStream inputStream = response.getContentAsStream();
		if (inputStream == null)
			inputStreamList.add(inputStream);
		return inputStream;
	}

}

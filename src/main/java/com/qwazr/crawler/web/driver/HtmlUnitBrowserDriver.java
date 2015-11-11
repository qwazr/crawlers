/**
 * Copyright 2014-2015 Emmanuel Keller / QWAZR
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
import com.qwazr.crawler.web.service.WebRequestDefinition;
import com.qwazr.utils.IOUtils;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebElement;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static com.qwazr.utils.json.JsonMapper.MAPPER;

public class HtmlUnitBrowserDriver extends BrowserDriver<HtmlUnitDriverWebClient> {

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
		super.close();
	}

	public WebClient getWebClient() {
		return driver.getWebClient();
	}

	public Page getEnclosedPage() {
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
	public WebResponse getWebResponse() {
		Page page = getEnclosedPage();
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

	public boolean isUnexpectedPage() {
		Page page = getEnclosedPage();
		if (page == null)
			return false;
		return page instanceof UnexpectedPage;
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

	public void saveResponseFile(File file) throws IOException {
		InputStream inputStream = getInputStream();
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

	public void saveResponse(String path) throws IOException {
		saveResponseFile(new File(path));
	}

	public Page request(String json) throws IOException {
		WebRequestDefinition webRequestDef = MAPPER.readValue(json, WebRequestDefinition.class);
		return getWebClient().getPage(webRequestDef.getNewWebRequest());
	}

	public WebElement convertNode(DomElement node) {
		return driver.convertNode(node);
	}

}

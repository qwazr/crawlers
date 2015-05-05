/**   
 * License Agreement for QWAZR
 *
 * Copyright (C) 2014-2015 OpenSearchServer Inc.
 * 
 * http://www.qwazr.com
 * 
 * This file is part of QWAZR.
 *
 * QWAZR is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * QWAZR is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with QWAZR. 
 *  If not, see <http://www.gnu.org/licenses/>.
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

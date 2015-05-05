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

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;

public class HtmlUnitDriverWebClient extends HtmlUnitDriver {

	public HtmlUnitDriverWebClient() {
		super(false);
	}

	public HtmlUnitDriverWebClient(Capabilities capabilities) {
		super(capabilities);
		if (capabilities != null) {
			String language = (String) capabilities
					.getCapability(AdditionalCapabilities.QWAZR_BROWER_LANGUAGE);
			if (language != null) {
				BrowserVersion version = getWebClient().getBrowserVersion();
				version.setBrowserLanguage(language);
				version.setSystemLanguage(language);
				version.setUserLanguage(language);
			}
		}
	}

	@Override
	protected WebClient newWebClient(BrowserVersion version) {
		return super.newWebClient(version.clone());
	}

	@Override
	public WebClient getWebClient() {
		return super.getWebClient();
	}

}

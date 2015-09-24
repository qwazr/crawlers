/**
 * Copyright 2014-2015 Emmanuel Keller / QWAZR
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/
package com.qwazr.crawler.web.driver;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.WebClient;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

public class HtmlUnitDriverWebClient extends HtmlUnitDriver {

    public HtmlUnitDriverWebClient() {
	super(false);
    }

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
	webClient.setRefreshHandler(null);
	webClient.getOptions().setThrowExceptionOnScriptError(false);
	webClient.setAjaxController(new NicelyResynchronizingAjaxController());
	return webClient;
    }

    @Override
    public WebClient getWebClient() {
	return super.getWebClient();
    }

}

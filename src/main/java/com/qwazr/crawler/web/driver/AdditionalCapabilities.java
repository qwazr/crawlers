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

import com.qwazr.crawler.web.service.WebRequestDefinition;
import org.openqa.selenium.WebElement;

import java.io.File;
import java.io.IOException;

public class AdditionalCapabilities {

	static final String QWAZR_BROWSER_LANGUAGE = "qwazr.browser.language";

	interface ResponseHeader {

		Integer getStatusCode();

		String getContentType();

		String getContentDisposition();

		String getContentDispositionFilename();
	}

	interface SafeText {

		String getTextSafe(WebElement webElement);
	}

	interface InnerHtml {

		String getInnerHtmlByXPath(String xPath);

	}

	interface SaveBinaryFile {

		void saveBinaryFile(File file) throws IOException;
	}

	interface SetAttribute {

		void setAttribute(WebElement element, String name, String value);
	}

	interface WebRequest {

		void request(WebRequestDefinition request);
	}

	interface All extends ResponseHeader, SafeText, InnerHtml, SaveBinaryFile, SetAttribute, WebRequest {

	}
}

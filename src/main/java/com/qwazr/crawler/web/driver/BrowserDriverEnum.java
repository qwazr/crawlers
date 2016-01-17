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

import com.qwazr.utils.IOUtils;
import org.apache.http.client.fluent.Request;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.safari.SafariDriver;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

public enum BrowserDriverEnum {

	chrome(ChromeBrowserDriver.class, "Chrome"),

	firefox(FirefoxBrowserDriver.class, "Firefox"),

	html_unit(HtmlUnitBrowserDriver.class, "HtmlUnit"),

	internet_explorer(InternetExplorerBrowserDriver.class, "Internet Exlorer"),

	phantomjs(PhantomJSBrowserDriver.class, "PhantomJS"),

	safari(SafariBrowserDriver.class, "Safari");

	private final Class<? extends BrowserDriver<?>> driverClass;

	private final String label;

	BrowserDriverEnum(Class<? extends BrowserDriver<?>> driverClass, String label) {
		this.driverClass = driverClass;
		this.label = label;
	}

	public BrowserDriver<?> getNewInstance(Capabilities cap)
			throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
			NoSuchMethodException, SecurityException {
		return driverClass.getConstructor(Capabilities.class).newInstance(cap);
	}

	public static BrowserDriverEnum find(String value, BrowserDriverEnum defaultValue) {
		if (value == null)
			return defaultValue;
		for (BrowserDriverEnum driver : BrowserDriverEnum.values())
			if (value.equalsIgnoreCase(driver.name()) || value.equalsIgnoreCase(driver.label))
				return driver;
		return defaultValue;
	}

	public String getLabel() {
		return label;
	}

	public String getName() {
		return name();
	}

	public static class ChromeBrowserDriver extends BrowserDriver<ChromeDriver> {

		public ChromeBrowserDriver(Capabilities capabilities) {
			super(BrowserDriverEnum.chrome, capabilities);
		}

		@Override
		protected ChromeDriver initialize(Capabilities capabilities) {
			if (capabilities == null)
				return new ChromeDriver();
			else
				return new ChromeDriver(capabilities);
		}
	}

	public static class FirefoxBrowserDriver extends BrowserDriver<FirefoxDriver> {

		public FirefoxBrowserDriver(Capabilities capabilities) {
			super(BrowserDriverEnum.firefox, capabilities);
		}

		@Override
		protected FirefoxDriver initialize(Capabilities capabilities) {
			if (capabilities == null)
				return new FirefoxDriver();
			else
				return new FirefoxDriver(capabilities);
		}
	}

	public static class InternetExplorerBrowserDriver extends BrowserDriver<InternetExplorerDriver> {

		public InternetExplorerBrowserDriver(Capabilities capabilities) {
			super(BrowserDriverEnum.internet_explorer, capabilities);
		}

		@Override
		protected InternetExplorerDriver initialize(Capabilities capabilities) {
			return new InternetExplorerDriver();
		}

	}

	public static class PhantomJSBrowserDriver extends BrowserDriver<PhantomJSDriver> {

		public PhantomJSBrowserDriver(Capabilities capabilities) {
			super(BrowserDriverEnum.phantomjs, capabilities);
		}

		@Override
		protected PhantomJSDriver initialize(Capabilities capabilities) {
			final PhantomJSDriver driver;
			if (capabilities == null)
				driver = new PhantomJSDriver();
			else
				driver = new PhantomJSDriver(capabilities);
			return driver;
		}

		private String JS_FIND_RESOURCE = "if (!this.resources) return; " + "for (var key in this.resources) { "
				+ "var res = this.resources[key]; " + "if (res.endReply) { "
				+ "if (res.endReply.url == arguments[0]) { " + "return res.endReply; " + " } " + " } " + " } ";

		public Integer getStatusCode() {
			Map<String, ?> result = (Map<String, ?>) driver.executePhantomJS(JS_FIND_RESOURCE, driver.getCurrentUrl());
			if (result == null)
				return null;
			Long statusCode = (Long) result.get("status");
			return statusCode == null ? null : statusCode.intValue();
		}

		public String getContentType() {
			Map<String, ?> result = (Map<String, ?>) driver.executePhantomJS(JS_FIND_RESOURCE, driver.getCurrentUrl());
			if (result == null)
				return null;
			String contentType = (String) result.get("contentType");
			if (contentType == null)
				return null;
			int i = contentType.indexOf(';');
			return i == -1 ? contentType : contentType.substring(0, i);
		}

		public void saveResponseFile(File file) throws IOException {
			if ("text/html".equals(getContentType()))
				IOUtils.writeStringAsFile(driver.getPageSource(), file);
			else
				Request.Get(driver.getCurrentUrl()).execute().saveContent(file);
		}
	}

	public static class SafariBrowserDriver extends BrowserDriver<SafariDriver> {

		public SafariBrowserDriver(Capabilities capabilities) {
			super(BrowserDriverEnum.safari, capabilities);
		}

		@Override
		public SafariDriver initialize(Capabilities capabilities) {
			if (capabilities == null)
				return new SafariDriver();
			else
				return new SafariDriver(capabilities);
		}

	}
}

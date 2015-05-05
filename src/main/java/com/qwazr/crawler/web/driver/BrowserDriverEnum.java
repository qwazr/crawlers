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

import java.lang.reflect.InvocationTargetException;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.safari.SafariDriver;

public enum BrowserDriverEnum {

	chrome(ChromeBrowserDriver.class, "Chrome"),

	firefox(FirefoxBrowserDriver.class, "Firefox"),

	html_unit(HtmlUnitBrowserDriver.class, "HtmlUnit"),

	internet_explorer(InternetExplorerBrowserDriver.class, "Internet Exlorer"),

	phantomjs(PhantomDriver.class, "PhantomJS"),

	safari(SafariBrowserDriver.class, "Safari");

	private final Class<? extends BrowserDriver<?>> driverClass;

	private final String label;

	private BrowserDriverEnum(Class<? extends BrowserDriver<?>> driverClass,
			String label) {
		this.driverClass = driverClass;
		this.label = label;
	}

	public BrowserDriver<?> getNewInstance(Capabilities cap)
			throws InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException,
			NoSuchMethodException, SecurityException {
		return driverClass.getConstructor(Capabilities.class).newInstance(cap);
	}

	public static BrowserDriverEnum find(String value,
			BrowserDriverEnum defaultValue) {
		if (value == null)
			return defaultValue;
		for (BrowserDriverEnum driver : BrowserDriverEnum.values())
			if (value.equalsIgnoreCase(driver.name())
					|| value.equalsIgnoreCase(driver.label))
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

	public static class FirefoxBrowserDriver extends
			BrowserDriver<FirefoxDriver> {

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

	public static class InternetExplorerBrowserDriver extends
			BrowserDriver<InternetExplorerDriver> {

		public InternetExplorerBrowserDriver(Capabilities capabilities) {
			super(BrowserDriverEnum.internet_explorer, capabilities);
		}

		@Override
		protected InternetExplorerDriver initialize(Capabilities capabilities) {
			return new InternetExplorerDriver();
		}

	}

	public static class PhantomDriver extends BrowserDriver<PhantomJSDriver> {

		public PhantomDriver(Capabilities capabilities) {
			super(BrowserDriverEnum.phantomjs, capabilities);
		}

		@Override
		protected PhantomJSDriver initialize(Capabilities capabilities) {
			if (capabilities == null)
				return new PhantomJSDriver();
			else
				return new PhantomJSDriver(capabilities);
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

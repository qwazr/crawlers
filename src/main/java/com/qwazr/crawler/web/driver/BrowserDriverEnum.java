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

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.safari.SafariDriver;

import java.lang.reflect.InvocationTargetException;

public enum BrowserDriverEnum {

	chrome(ChromeDriver.class, "Chrome"),

	firefox(FirefoxDriver.class, "Firefox"),

	html_unit(HtmlUnitBrowserDriver.class, "HtmlUnit"),

	internet_explorer(InternetExplorerDriver.class, "Internet Exlorer"),

	phantomjs(PhantomJSBrowserDriver.class, "PhantomJS"),

	safari(SafariDriver.class, "Safari");

	private final Class<? extends WebDriver> driverClass;

	private final String label;

	BrowserDriverEnum(Class<? extends WebDriver> driverClass, String label) {
		this.driverClass = driverClass;
		this.label = label;
	}

	public WebDriver getNewInstance(Capabilities cap)
			throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
			NoSuchMethodException, SecurityException {
		if (cap == null)
			return driverClass.newInstance();
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

}

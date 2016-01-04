/**
 * Copyright 2014-2016 Emmanuel Keller / QWAZR
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

    BrowserDriverEnum(Class<? extends BrowserDriver<?>> driverClass, String label) {
	this.driverClass = driverClass;
	this.label = label;
    }

    public BrowserDriver<?> getNewInstance(Capabilities cap)
		    throws InstantiationException, IllegalAccessException, IllegalArgumentException,
		    InvocationTargetException, NoSuchMethodException, SecurityException {
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

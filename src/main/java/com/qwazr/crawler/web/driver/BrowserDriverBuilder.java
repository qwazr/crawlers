/*
 * Copyright 2015-2017 Emmanuel Keller / QWAZR
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
 */
package com.qwazr.crawler.web.driver;

import com.qwazr.crawler.web.ProxyDefinition;
import com.qwazr.crawler.web.WebCrawlDefinition;
import org.apache.commons.lang3.RandomUtils;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.BrowserType;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This class is responsible of creating a WebDriver build the capabilities by
 * reading the crawl definition (CrawlJson).
 */
public class BrowserDriverBuilder {

	private final WebCrawlDefinition crawlDefinition;

	public BrowserDriverBuilder(WebCrawlDefinition crawlDefinition) {
		this.crawlDefinition = crawlDefinition;
	}

	private DesiredCapabilities checkCapabilities(DesiredCapabilities capabilities) {
		if (capabilities == null)
			capabilities = new DesiredCapabilities();
		return capabilities;
	}

	private ProxyDefinition selectProxy() {
		if (crawlDefinition.proxy == null && (crawlDefinition.proxies == null || crawlDefinition.proxies.isEmpty()))
			return null;
		final List<ProxyDefinition> activeProxies = new ArrayList<>();
		if (crawlDefinition.proxy != null)
			activeProxies.add(crawlDefinition.proxy);
		if (crawlDefinition.proxies != null)
			for (ProxyDefinition proxy : crawlDefinition.proxies)
				if (proxy.enabled == null || proxy.enabled)
					activeProxies.add(proxy);
		if (activeProxies.size() == 0)
			return null;
		return activeProxies.get(RandomUtils.nextInt(0, activeProxies.size()));
	}

	public BrowserDriver build() throws ReflectiveOperationException, SecurityException {
		BrowserDriverEnum browserType = BrowserDriverEnum.html_unit;

		final ProxyDefinition proxyDef;

		DesiredCapabilities capabilities = null;

		if (crawlDefinition != null) {

			// Choose a browser type
			if (crawlDefinition.browserType != null)
				browserType = crawlDefinition.browserType;

			proxyDef = selectProxy();

			// Setup the proxy
			if (proxyDef != null) {
				capabilities = checkCapabilities(capabilities);
				org.openqa.selenium.Proxy proxy = new Proxy();
				if (proxyDef.httpProxy != null)
					proxy.setHttpProxy(proxyDef.httpProxy);
				if (proxyDef.ftpProxy != null)
					proxy.setFtpProxy(proxyDef.ftpProxy);
				if (proxyDef.sslProxy != null)
					proxy.setSslProxy(proxyDef.sslProxy);
				if (proxyDef.socksProxy != null)
					proxy.setSocksProxy(proxyDef.socksProxy);
				if (proxyDef.socksUsername != null)
					proxy.setSocksUsername(proxyDef.socksUsername);
				if (proxyDef.socksPassword != null)
					proxy.setSocksPassword(proxyDef.socksPassword);
				if (proxyDef.noProxy != null)
					proxy.setNoProxy(proxyDef.noProxy);
				if (proxyDef.proxyAutoconfigUrl != null)
					proxy.setProxyAutoconfigUrl(proxyDef.proxyAutoconfigUrl);
				capabilities.setCapability(CapabilityType.PROXY, proxy);
			}

			// Setup the language
			if (crawlDefinition.browserLanguage != null) {
				capabilities = checkCapabilities(capabilities);
				capabilities.setCapability(AdditionalCapabilities.QWAZR_BROWSER_LANGUAGE,
						crawlDefinition.browserLanguage);
				if (browserType == BrowserDriverEnum.phantomjs)
					capabilities.setCapability(
							PhantomJSDriverService.PHANTOMJS_PAGE_CUSTOMHEADERS_PREFIX + "Accept-Language",
							crawlDefinition.browserLanguage);
			}

			// Download images
			if (crawlDefinition.downloadImages != null) {
				capabilities = checkCapabilities(capabilities);
				if (browserType == BrowserDriverEnum.phantomjs)
					capabilities.setCapability(PhantomJSDriverService.PHANTOMJS_PAGE_SETTINGS_PREFIX + "loadImages",
							crawlDefinition.downloadImages);
			}

			// Web security
			if (crawlDefinition.webSecurity != null) {
				capabilities = checkCapabilities(capabilities);
				if (browserType == BrowserDriverEnum.phantomjs)
					capabilities.setCapability(
							PhantomJSDriverService.PHANTOMJS_PAGE_SETTINGS_PREFIX + "webSecurityEnabled",
							crawlDefinition.webSecurity);
			}

			// Choose a browser name
			if (crawlDefinition.browserName != null) {
				capabilities = checkCapabilities(capabilities);
				capabilities.setBrowserName(crawlDefinition.browserName);
			}

			if (crawlDefinition.browserVersion != null) {
				capabilities = checkCapabilities(capabilities);
				capabilities.setVersion(crawlDefinition.browserVersion);
			}

			// Javascript capability
			if (crawlDefinition.javascriptEnabled != null) {
				capabilities = checkCapabilities(capabilities);
				capabilities.setJavascriptEnabled(crawlDefinition.javascriptEnabled);
				if (browserType == BrowserDriverEnum.phantomjs)
					capabilities.setCapability(
							PhantomJSDriverService.PHANTOMJS_PAGE_SETTINGS_PREFIX + "javascriptEnabled",
							crawlDefinition.javascriptEnabled);

			}

		} else
			proxyDef = null;

		if (browserType == BrowserDriverEnum.phantomjs) {
			capabilities = checkCapabilities(capabilities);
			capabilities.setCapability(PhantomJSDriverService.PHANTOMJS_CLI_ARGS,
					new String[] { "--webdriver-loglevel=none", "--ignore-ssl-errors=true", "--ssl-protocol=any" });
		}

		if (browserType == BrowserDriverEnum.html_unit) {
			capabilities = checkCapabilities(capabilities);
			capabilities.setBrowserName(BrowserType.HTMLUNIT);
		}

		final WebDriver driver = browserType.getNewInstance(capabilities);
		try {
			final BrowserDriver browserDriver = new BrowserDriver(browserType, driver, proxyDef);
			browserDriver.setTimeouts(crawlDefinition.implicitlyWait, crawlDefinition.pageLoadTimeout,
					crawlDefinition.scriptTimeout);

			if (crawlDefinition.cookies != null)
				for (Map.Entry<String, String> cookie : crawlDefinition.cookies.entrySet())
					driver.manage().addCookie(new Cookie(cookie.getKey(), cookie.getValue()));

			return browserDriver;
		} catch (Exception e) {
			driver.quit();
			throw e;
		}
	}
}

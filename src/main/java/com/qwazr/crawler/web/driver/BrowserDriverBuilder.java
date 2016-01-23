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

import com.qwazr.crawler.web.service.WebCrawlDefinition;
import org.apache.commons.lang3.RandomUtils;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
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

	private WebCrawlDefinition.ProxyDefinition selectProxy() {
		if (crawlDefinition.proxy == null && (crawlDefinition.proxies == null || crawlDefinition.proxies.isEmpty()))
			return null;
		final List<WebCrawlDefinition.ProxyDefinition> activeProxies = new ArrayList<>();
		if (crawlDefinition.proxy != null)
			activeProxies.add(crawlDefinition.proxy);
		if (crawlDefinition.proxies != null)
			for (WebCrawlDefinition.ProxyDefinition proxy : crawlDefinition.proxies)
				if (proxy.enabled == null || proxy.enabled)
					activeProxies.add(proxy);
		if (activeProxies.size() == 0)
			return null;
		return activeProxies.get(RandomUtils.nextInt(0, activeProxies.size()));
	}

	public BrowserDriver<?> build() throws ReflectiveOperationException, SecurityException {
		BrowserDriverEnum browserType = BrowserDriverEnum.html_unit;

		final WebCrawlDefinition.ProxyDefinition proxyDef;

		DesiredCapabilities capabilities = null;

		if (crawlDefinition != null) {

			// Choose a browser type
			if (crawlDefinition.browser_type != null)
				browserType = crawlDefinition.browser_type;

			proxyDef = selectProxy();

			// Setup the proxy
			if (proxyDef != null) {
				capabilities = checkCapabilities(capabilities);
				org.openqa.selenium.Proxy proxy = new Proxy();
				if (proxyDef.http_proxy != null)
					proxy.setHttpProxy(proxyDef.http_proxy);
				if (proxyDef.ftp_proxy != null)
					proxy.setFtpProxy(proxyDef.ftp_proxy);
				if (proxyDef.ssl_proxy != null)
					proxy.setSslProxy(proxyDef.ssl_proxy);
				if (proxyDef.socks_proxy != null)
					proxy.setSocksProxy(proxyDef.socks_proxy);
				if (proxyDef.socks_username != null)
					proxy.setSocksUsername(proxyDef.socks_username);
				if (proxyDef.socks_password != null)
					proxy.setSocksPassword(proxyDef.socks_password);
				if (proxyDef.no_proxy != null)
					proxy.setNoProxy(proxyDef.no_proxy);
				if (proxyDef.proxy_autoconfig_url != null)
					proxy.setProxyAutoconfigUrl(proxyDef.proxy_autoconfig_url);
				capabilities.setCapability(CapabilityType.PROXY, proxy);
			}

			// Setup the language
			if (crawlDefinition.browser_language != null) {
				capabilities = checkCapabilities(capabilities);
				capabilities
						.setCapability(AdditionalCapabilities.QWAZR_BROWSER_LANGUAGE, crawlDefinition.browser_language);
				if (browserType == BrowserDriverEnum.phantomjs)
					capabilities.setCapability(
							PhantomJSDriverService.PHANTOMJS_PAGE_CUSTOMHEADERS_PREFIX + "Accept-Language",
							crawlDefinition.browser_language);
			}

			// Download images
			if (crawlDefinition.download_images != null) {
				capabilities = checkCapabilities(capabilities);
				if (browserType == BrowserDriverEnum.phantomjs)
					capabilities.setCapability(PhantomJSDriverService.PHANTOMJS_PAGE_SETTINGS_PREFIX + "loadImages",
							crawlDefinition.download_images);
			}

			// Web security
			if (crawlDefinition.web_security != null) {
				capabilities = checkCapabilities(capabilities);
				if (browserType == BrowserDriverEnum.phantomjs)
					capabilities
							.setCapability(PhantomJSDriverService.PHANTOMJS_PAGE_SETTINGS_PREFIX + "webSecurityEnabled",
									crawlDefinition.web_security);
			}

			// Choose a browser name
			if (crawlDefinition.browser_name != null) {
				capabilities = checkCapabilities(capabilities);
				capabilities.setBrowserName(crawlDefinition.browser_name);
			}

			if (crawlDefinition.browser_version != null) {
				capabilities = checkCapabilities(capabilities);
				capabilities.setVersion(crawlDefinition.browser_version);
			}

			// Javascript capability
			if (crawlDefinition.javascript_enabled != null) {
				capabilities = checkCapabilities(capabilities);
				capabilities.setJavascriptEnabled(crawlDefinition.javascript_enabled);
				if (browserType == BrowserDriverEnum.phantomjs)
					capabilities
							.setCapability(PhantomJSDriverService.PHANTOMJS_PAGE_SETTINGS_PREFIX + "javascriptEnabled",
									crawlDefinition.javascript_enabled);

			}

		} else
			proxyDef = null;

		if (browserType == BrowserDriverEnum.phantomjs) {
			capabilities = checkCapabilities(capabilities);
			String[] phantomArgs = new String[] { "--webdriver-loglevel=NONE" };
			capabilities.setCapability(PhantomJSDriverService.PHANTOMJS_CLI_ARGS, phantomArgs);
		}

		final BrowserDriver driver = browserType.getNewInstance(capabilities);
		driver.setProxy(proxyDef);
		driver.setTimeouts(crawlDefinition.implicitly_wait, crawlDefinition.page_load_timeout,
				crawlDefinition.script_timeout);

		if (crawlDefinition.cookies != null)
			for (Map.Entry<String, String> cookie : crawlDefinition.cookies.entrySet())
				driver.manage().addCookie(new Cookie(cookie.getKey(), cookie.getValue()));

		return driver;
	}
}

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
import com.qwazr.utils.StringUtils;
import org.openqa.selenium.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.css.sac.CSSException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public abstract class BrowserDriver<T extends WebDriver> implements WebDriver, Closeable {

	private static final Logger logger = LoggerFactory.getLogger(BrowserDriver.class);

	private WebCrawlDefinition.ProxyDefinition currentProxy;
	protected final BrowserDriverEnum type;
	protected T driver = null;

	protected BrowserDriver(BrowserDriverEnum type, Capabilities cap) {
		this.type = type;
		driver = initialize(cap);
		Timeouts timeouts = driver.manage().timeouts();
		timeouts.implicitlyWait(1, TimeUnit.MINUTES);
		timeouts.setScriptTimeout(2, TimeUnit.MINUTES);
		timeouts.pageLoadTimeout(3, TimeUnit.MINUTES);
	}

	protected abstract T initialize(Capabilities cap);

	@Override
	public void close() {
		if (driver == null)
			return;
		driver.quit();
		driver = null;
	}

	@Override
	final public void get(String sUrl) {
		driver.get(sUrl);
	}

	public BrowserDriverEnum getType() {
		return type;
	}

	public Object executeScript(String javascript, boolean faultTolerant, Object... objects) {
		try {
			if (!(driver instanceof JavascriptExecutor))
				throw new WebDriverException("The Web driver does not support javascript execution");
			JavascriptExecutor js = (JavascriptExecutor) driver;
			return js.executeScript(javascript, objects);
		} catch (WebDriverException e) {
			if (!faultTolerant)
				throw e;
			logger.warn(e.getMessage(), e);
			return null;
		}
	}

	final public BufferedImage getScreenshot() throws IOException {
		if (!(driver instanceof TakesScreenshot))
			throw new WebDriverException("This browser driver does not support screenshot");
		TakesScreenshot takesScreenshot = (TakesScreenshot) driver;
		byte[] data = takesScreenshot.getScreenshotAs(OutputType.BYTES);
		return ImageIO.read(new ByteArrayInputStream(data));
	}

	@Override
	final public String getTitle() {
		return driver.getTitle();
	}

	final public void setSize(int width, int height) {
		driver.manage().window().setSize(new Dimension(width, height));
	}

	final public void setTimeouts(Integer impWait, Integer pageLoad, Integer script) {
		Timeouts timeOuts = driver.manage().timeouts();
		if (impWait != null)
			timeOuts.implicitlyWait(impWait, TimeUnit.SECONDS);
		if (pageLoad != null)
			timeOuts.pageLoadTimeout(pageLoad, TimeUnit.SECONDS);
		if (script != null)
			timeOuts.setScriptTimeout(script, TimeUnit.SECONDS);
	}

	final void setProxy(WebCrawlDefinition.ProxyDefinition proxy) {
		this.currentProxy = proxy;
	}

	final public WebCrawlDefinition.ProxyDefinition getProxy() {
		return this.currentProxy;
	}

	@Override
	final public List<WebElement> findElements(By by) {
		return driver.findElements(by);
	}

	@Override
	final public String getWindowHandle() {
		return driver.getWindowHandle();
	}

	@Override
	final public String getCurrentUrl() {
		return driver.getCurrentUrl();
	}

	@Override
	final public WebElement findElement(By by) {
		return driver.findElement(by);
	}

	@Override
	final public String getPageSource() {
		return driver.getPageSource();
	}

	@Override
	final public void quit() {
		driver.quit();
	}

	@Override
	final public Set<String> getWindowHandles() {
		return driver.getWindowHandles();
	}

	@Override
	final public TargetLocator switchTo() {
		return driver.switchTo();
	}

	@Override
	final public Navigation navigate() {
		return driver.navigate();
	}

	@Override
	final public Options manage() {
		return driver.manage();
	}

	/**
	 * Fill a list with all the href attributes found in a tag, relative to the
	 * given rootElement
	 *
	 * @param searchContext  the root of the search
	 * @param hrefCollection the collection filled with the href content
	 */
	public void findLinks(SearchContext searchContext, Collection<String> hrefCollection) {
		extractLinks(searchContext, hrefCollection, "a", "href", "data-href");
		extractLinks(searchContext, hrefCollection, "div", "data-href");
		extractLinks(searchContext, hrefCollection, "frame", "src");
	}

	public void findRssItemLinks(SearchContext searchContext, Collection<String> linkCollection) {
		List<WebElement> channels = driver.findElements(By.tagName("channel"));
		for (WebElement channel : channels) {
			List<WebElement> items = channel.findElements(By.tagName("item"));
			for (WebElement item : items) {
				List<WebElement> links = item.findElements(By.tagName("link"));
				for (WebElement link : links) {
					linkCollection.add(link.getText());
				}
			}
		}
	}

	public List<String> getRssItemLinks() {
		ArrayList<String> links = new ArrayList<String>();
		findRssItemLinks(driver, links);
		return links;
	}

	public String getSnippet(SearchContext searchContext, int sizeLimit) {
		List<WebElement> elements = searchContext.findElements(By.tagName("p"));
		if (elements == null)
			return null;
		StringBuilder sb = new StringBuilder();
		for (WebElement element : elements) {
			String text = null;
			try {
				text = element.getText();
			} catch (CSSException e) {
				if (logger.isWarnEnabled())
					logger.warn("Cannot extract snippet: " + e.getMessage(), e);
			}
			if (text == null)
				continue;
			text = StringUtils.join(StringUtils.split(text, " \r\n\t"), ' ');
			sb.append(text);
			if (!text.endsWith("."))
				sb.append(' ');
			sb.append(' ');
			if (sb.length() > sizeLimit)
				break;
		}
		if (sb.length() <= sizeLimit)
			return sb.toString().trim();
		int i = 0;
		int last = -1;
		for (; ; ) {
			i = sb.indexOf(" ", i + 1);
			if (i == -1 || i > sizeLimit)
				break;
			last = i;
		}
		if (last == -1)
			last = sizeLimit;
		return sb.substring(0, last) + "…";
	}

	private void extractLinks(SearchContext searchContext, Collection<String> hrefCollection, String tag,
			String... attrs) {

		// Let's look for the a tags
		List<WebElement> links = searchContext.findElements(By.tagName(tag));
		if (links == null || links.isEmpty())
			return;

		// Building the URI list
		for (WebElement link : links) {
			for (String attr : attrs) {
				String href = link.getAttribute(attr);
				if (href != null) {
					href = StringUtils.replace(href, " ", "+");
					hrefCollection.add(href);
					break;
				}
			}
		}
	}

	private WebElement findElementBy(By by) {
		try {
			return driver.findElement(by);
		} catch (NoSuchElementException e) {
			return null;
		}
	}

	public String getInnerHtmlByXPath(String xPath) {
		WebElement element = findElementBy(By.xpath(xPath));
		if (element == null)
			return null;
		return element.getAttribute("innerHTML");
	}

	public WebElement findElementByXPath(String xPath) {
		return findElementBy(By.xpath(xPath));
	}

	public WebElement findElementByTagName(String tagName) {
		return findElementBy(By.tagName(tagName));
	}

	public WebElement findElementByCssSelector(String cssSelector) {
		return findElementBy(By.cssSelector(cssSelector));
	}

	public List<WebElement> findElementsByXPath(String xPath) {
		return driver.findElements(By.xpath(xPath));
	}

	public List<WebElement> findElementsByTagName(String tagName) {
		return driver.findElements(By.tagName(tagName));
	}

	public List<WebElement> findElementsByCssSelector(String cssSelector) {
		return driver.findElements(By.cssSelector(cssSelector));
	}

}

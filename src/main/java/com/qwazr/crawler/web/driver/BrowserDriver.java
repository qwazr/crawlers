/**
 * Copyright 2014-2015 Emmanuel Keller / QWAZR
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/
package com.qwazr.crawler.web.driver;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import org.openqa.selenium.By;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BrowserDriver<T extends WebDriver> implements WebDriver,
		Closeable {

	private static final Logger logger = LoggerFactory
			.getLogger(BrowserDriver.class);

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

	public Object executeScript(String javascript, boolean faultTolerant,
			Object... objects) {
		try {
			if (!(driver instanceof JavascriptExecutor))
				throw new WebDriverException(
						"The Web driver does not support javascript execution");
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
			throw new WebDriverException(
					"This browser driver does not support screenshot");
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

	final public void setTimeouts(Integer pageLoad, Integer script) {
		Timeouts timeOuts = driver.manage().timeouts();
		if (pageLoad != null)
			timeOuts.pageLoadTimeout(pageLoad, TimeUnit.SECONDS);
		if (script != null)
			timeOuts.setScriptTimeout(script, TimeUnit.SECONDS);
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
}

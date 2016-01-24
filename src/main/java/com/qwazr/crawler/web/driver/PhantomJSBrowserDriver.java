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
 */
package com.qwazr.crawler.web.driver;

import com.qwazr.utils.process.ProcessUtils;
import org.apache.commons.lang3.SystemUtils;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

public class PhantomJSBrowserDriver extends PhantomJSDriver implements AdditionalCapabilities.ResponseHeader {

	protected static final Logger logger = LoggerFactory.getLogger(PhantomJSBrowserDriver.class);

	private Number pid;

	public PhantomJSBrowserDriver() {
		super();
		pid = getPidOrDie();
	}

	public PhantomJSBrowserDriver(Capabilities capabilities) {
		super(capabilities);
		pid = getPidOrDie();
	}

	private Number getPidOrDie() {
		try {
			Object object = executePhantomJS("var system = require('system'); return system.pid;");
			if (object instanceof Number)
				return (Number) object;
		} catch (Exception e) {
			quit();
		}
		return null;
	}

	@Override
	public void quit() {
		super.quit();
		if (pid == null)
			return;
		// Paranoid quit. Check if the PhantomJS process is still running
		if (SystemUtils.IS_OS_WINDOWS) {
			try {
				ProcessUtils.forceKill(pid);
				pid = null;
			} catch (IOException | InterruptedException e) {
				if (logger.isWarnEnabled())
					logger.warn("Cannot kill PhantomJS PID " + pid, e);
			}
			return;
		}
		if (SystemUtils.IS_OS_UNIX) {
			if (!processIsRunning()) {
				pid = null;
				return;
			}
			try {
				ProcessUtils.kill(pid);
			} catch (IOException | InterruptedException e) {
				if (logger.isWarnEnabled())
					logger.warn("Cannot check if PhantomJS PID is running " + pid, e);
			}
			if (!processIsRunning()) {
				pid = null;
				return;
			}
			try {
				ProcessUtils.forceKill(pid);
			} catch (IOException | InterruptedException e) {
				if (logger.isWarnEnabled())
					logger.warn("Cannot check if PhantomJS PID is running " + pid, e);
			}
		}
	}

	private boolean processIsRunning() {
		if (pid == null)
			return false;
		try {
			if (logger.isInfoEnabled())
				logger.info("Check if PhantomJS PID is running: " + pid);
			return ProcessUtils.isRunning(pid);
		} catch (IOException | InterruptedException e) {
			if (logger.isWarnEnabled())
				logger.warn("Cannot check if PID is running " + pid, e);
			return true;
		}
	}

	private String JS_FIND_RESOURCE =
			"if (!this.resources) return; " + "for (var key in this.resources) { " + "var res = this.resources[key]; "
					+ "if (res.endReply) { " + "if (res.endReply.url == arguments[0]) { " + "return res.endReply; "
					+ " } " + " } " + " } ";

	@Override
	public Integer getStatusCode() {
		Map<String, ?> result = (Map<String, ?>) executePhantomJS(JS_FIND_RESOURCE, getCurrentUrl());
		if (result == null)
			return null;
		Long statusCode = (Long) result.get("status");
		return statusCode == null ? null : statusCode.intValue();
	}

	@Override
	public String getContentType() {
		Map<String, ?> result = (Map<String, ?>) executePhantomJS(JS_FIND_RESOURCE, getCurrentUrl());
		if (result == null)
			return null;
		String contentType = (String) result.get("contentType");
		if (contentType == null)
			return null;
		int i = contentType.indexOf(';');
		return i == -1 ? contentType : contentType.substring(0, i);
	}

}

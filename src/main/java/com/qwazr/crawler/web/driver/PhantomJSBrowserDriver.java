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

import com.qwazr.utils.http.HttpUtils;
import com.qwazr.utils.process.ProcessUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.http.entity.mime.MIME;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class PhantomJSBrowserDriver extends PhantomJSDriver implements AdditionalCapabilities.ResponseHeader {

	protected static final Logger logger = LoggerFactory.getLogger(PhantomJSBrowserDriver.class);

	private final AtomicReference<Number> pid = new AtomicReference<>(null);

	private String requestedUrl = null;
	private Map<String, ?> endEntry = null;
	private Map<String, String> headers = null;

	public PhantomJSBrowserDriver() {
		super();
		pid.set(getPidOrDie());
	}

	public PhantomJSBrowserDriver(Capabilities capabilities) {
		super(capabilities);
		pid.set(getPidOrDie());
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
		if (pid.get() == null)
			return;
		// Paranoid quit. Check if the PhantomJS process is still running
		if (SystemUtils.IS_OS_WINDOWS) {
			try {
				ProcessUtils.forceKill(pid.getAndSet(null));
			} catch (IOException | InterruptedException e) {
				if (logger.isWarnEnabled())
					logger.warn("Cannot kill PhantomJS PID " + pid.get(), e);
			}
			return;
		}
		if (SystemUtils.IS_OS_UNIX) {
			if (!processIsRunning()) {
				pid.set(null);
				return;
			}
			try {
				ProcessUtils.kill(pid.get());
			} catch (IOException | InterruptedException e) {
				if (logger.isWarnEnabled())
					logger.warn("Cannot check if PhantomJS PID is running " + pid.get(), e);
			}
			if (!processIsRunning()) {
				pid.set(null);
				return;
			}
			try {
				ProcessUtils.forceKill(pid.get());
			} catch (IOException | InterruptedException e) {
				if (logger.isWarnEnabled())
					logger.warn("Cannot check if PhantomJS PID is running " + pid.get(), e);
			}
		}
	}

	private boolean processIsRunning() {
		if (pid.get() == null)
			return false;
		try {
			if (logger.isInfoEnabled())
				logger.info("Check if PhantomJS PID is running: " + pid.get());
			return ProcessUtils.isRunning(pid.get());
		} catch (IOException | InterruptedException e) {
			if (logger.isWarnEnabled())
				logger.warn("Cannot check if PID is running " + pid.get(), e);
			return true;
		}
	}

	private String JS_FIND_END_ENTRY =
			"if (!this.resources) return; " + "for (var key in this.resources) { " + "var res = this.resources[key]; "
					+ "if (res.endReply) { " + "if (res.endReply.url == arguments[0]) { " + "return res.endReply; "
					+ " } " + " } " + " } ";

	private boolean findEndEntry(String url) {
		endEntry = (Map<String, ?>) executePhantomJS(JS_FIND_END_ENTRY, url);
		headers = null;
		return endEntry != null && endEntry.size() > 0;
	}

	@Override
	public void get(String url) {
		requestedUrl = url;
		super.get(url);
		final String currentUrl = super.getCurrentUrl();
		int test = 60;
		boolean found = false;
		try {
			while (--test > 0) {
				found = findEndEntry(currentUrl) || findEndEntry(requestedUrl);
				if (found)
					break;
				Thread.sleep(2000);
			}
		} catch (InterruptedException e) {
		}
		if (!found)
			logger.warn("PhantomJSDriver: Resource EndEntry not found: " + requestedUrl);
	}

	@Override
	public String getCurrentUrl() {
		String url = null;
		if (endEntry != null)
			url = (String) endEntry.get("url");
		if (url != null)
			return url;
		return super.getCurrentUrl();
	}

	@Override
	public Integer getStatusCode() {
		if (endEntry == null)
			return null;
		Number statusCode = (Number) endEntry.get("status");
		return statusCode == null ? null : statusCode.intValue();
	}

	@Override
	public String getContentType() {
		if (endEntry == null)
			return null;
		String contentType = (String) endEntry.get("contentType");
		if (contentType == null)
			return null;
		int i = contentType.indexOf(';');
		return i == -1 ? contentType : contentType.substring(0, i);
	}

	private Map<String, String> getHeaders() {
		if (endEntry == null)
			return null;
		if (headers != null)
			return headers;
		List<Map<String, ?>> entries = (List<Map<String, ?>>) endEntry.get("headers");
		if (entries == null)
			return null;
		final Map<String, String> headers = new LinkedHashMap<>();
		entries.forEach(new Consumer<Map<String, ?>>() {
			@Override
			public void accept(Map<String, ?> map) {
				Object name = map.get("name");
				Object value = map.get("value");
				if (name == null || value == null)
					return;
				headers.put(name.toString().trim().toLowerCase(), value.toString());
			}
		});
		return this.headers = headers;
	}

	private String getHeader(String name) {
		Map<String, String> headers = getHeaders();
		if (headers == null)
			return null;
		return headers.get(name.trim().toLowerCase());
	}

	@Override
	public String getContentDisposition() {
		if (endEntry == null)
			return null;
		return getHeader(MIME.CONTENT_DISPOSITION);
	}

	@Override
	public String getContentDispositionFilename() {
		return HttpUtils.getHeaderParameter(getContentDisposition(), "filename");
	}

}

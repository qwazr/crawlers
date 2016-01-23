package com.qwazr.crawler.web.driver;

import com.qwazr.utils.IOUtils;
import org.apache.http.client.fluent.Request;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.phantomjs.PhantomJSDriver;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class PhantomJSBrowserDriver extends BrowserDriver<PhantomJSDriver> {

	public PhantomJSBrowserDriver(Capabilities capabilities) {
		super(BrowserDriverEnum.phantomjs, capabilities);
	}

	@Override
	protected PhantomJSDriver initialize(Capabilities capabilities) {
		return new PhantomJSDriver(capabilities);
	}

	private String JS_FIND_RESOURCE =
			"if (!this.resources) return; " + "for (var key in this.resources) { " + "var res = this.resources[key]; "
					+ "if (res.endReply) { " + "if (res.endReply.url == arguments[0]) { " + "return res.endReply; "
					+ " } " + " } " + " } ";

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

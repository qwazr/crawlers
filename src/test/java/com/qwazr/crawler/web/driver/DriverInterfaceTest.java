package com.qwazr.crawler.web.driver;

import com.qwazr.crawler.web.WebCrawlDefinition;
import com.qwazr.crawler.web.WebRequestDefinition;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class DriverInterfaceTest {

	@Test
	public void test() throws IOException {
		final DriverInterface driver = DriverInterface.of(WebCrawlDefinition.of().build());
		final WebRequestDefinition request = WebRequestDefinition.of("http://www.opensearchserver.com/").build();
		Assert.assertNotNull(driver.body(request));
	}
}

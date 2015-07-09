package com.qwazr.crawler.web;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.qwazr.cluster.manager.ClusterManager;
import com.qwazr.crawler.web.client.WebCrawlerMultiClient;
import com.qwazr.crawler.web.driver.BrowserDriver;
import com.qwazr.crawler.web.driver.BrowserDriverBuilder;
import com.qwazr.crawler.web.service.WebCrawlDefinition;
import com.qwazr.tools.AbstractTool;
import com.qwazr.utils.IOUtils;
import com.qwazr.utils.json.JsonMapper;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;


@JsonIgnoreProperties(ignoreUnknown = true)
public class WebCrawlerTool extends AbstractTool {

	public WebCrawlDefinition config;

	@Override
	public void load(File parentDir) {
	}

	@Override
	public void unload() {
	}

	/**
	 * Create a new WebCrawler client instance.
	 * This API queries the cluster to get the current active node for the WebCrawler service.
	 *
	 * @return a new WebCrawlerMultiClient instance
	 * @throws URISyntaxException
	 */
	public WebCrawlerMultiClient getNewWebCrawlerClient() throws URISyntaxException {
		return getNewWebCrawlerClient(null);
	}

	/**
	 * Create a new WebCrawler client instance.
	 * This API queries the cluster to get the current active node for the WebCrawler service.
	 *
	 * @param msTimeout the default timeout used by the client
	 * @return a new WebCrawlerMultiClient instance
	 * @throws URISyntaxException
	 */
	public WebCrawlerMultiClient getNewWebCrawlerClient(Integer msTimeout)
			throws URISyntaxException {
		return new WebCrawlerMultiClient(ClusterManager.INSTANCE
				.getClusterClient().getActiveNodes(
						WebCrawlerServer.SERVICE_NAME_WEBCRAWLER), msTimeout);
	}

	public WebCrawlDefinition getNewWebCrawlDefinition() {
		return config == null ? new WebCrawlDefinition() : (WebCrawlDefinition) config.clone();
	}

	public BrowserDriver getNewWebDriver(IOUtils.CloseableContext context, String json) throws ReflectiveOperationException, SecurityException, IOException {
		WebCrawlDefinition webCrawlDef = JsonMapper.MAPPER.readValue(json, WebCrawlDefinition.class);
		return new BrowserDriverBuilder(webCrawlDef).build();
	}
}

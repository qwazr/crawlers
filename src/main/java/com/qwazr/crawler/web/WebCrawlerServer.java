/**
 * Copyright 2014-2015 OpenSearchServer Inc.
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
package com.qwazr.crawler.web;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import javax.servlet.ServletException;
import javax.ws.rs.ApplicationPath;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;

import com.qwazr.cluster.ClusterServer;
import com.qwazr.cluster.manager.ClusterManager;
import com.qwazr.crawler.web.manager.WebCrawlerManager;
import com.qwazr.crawler.web.service.WebCrawlerServiceImpl;
import com.qwazr.job.script.ScriptManager;
import com.qwazr.job.script.ScriptServiceImpl;
import com.qwazr.utils.server.AbstractServer;
import com.qwazr.utils.server.RestApplication;
import com.qwazr.utils.server.ServletApplication;

public class WebCrawlerServer extends AbstractServer {

	public final static String SERVICE_NAME_WEBCRAWLER = "webcrawler";

	private final static ServerDefinition serverDefinition = new ServerDefinition();
	static {
		serverDefinition.defaultWebApplicationTcpPort = 9097;
		serverDefinition.mainJarPath = "qwazr-crawler.jar";
	}

	private WebCrawlerServer() {
		super(serverDefinition);
	}

	@ApplicationPath("/")
	public static class WebCrawlerApplication extends RestApplication {

		@Override
		public Set<Class<?>> getClasses() {
			Set<Class<?>> classes = super.getClasses();
			classes.add(WebCrawlerServiceImpl.class);
			classes.add(ScriptServiceImpl.class);
			return classes;
		}
	}

	@Override
	public void commandLine(CommandLine cmd) throws IOException, ParseException {
	}

	public static void load(AbstractServer server) throws IOException {
		File dataDir = server.getCurrentDataDir();
		WebCrawlerManager.load(server, dataDir);
	}

	@Override
	public void load() throws IOException {
		ClusterServer.load(getWebServicePublicAddress(), getCurrentDataDir(),
				null);
		ScriptManager.load(getCurrentDataDir());
		load(this);
	}

	public static void main(String[] args) throws IOException, ParseException,
			ServletException {
		new WebCrawlerServer().start(args);
		ClusterManager.INSTANCE.registerMe(SERVICE_NAME_WEBCRAWLER);
	}

	@Override
	protected RestApplication getRestApplication() {
		return new WebCrawlerApplication();
	}

	@Override
	protected ServletApplication getServletApplication() {
		return null;
	}

}

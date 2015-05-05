/**   
 * License Agreement for QWAZR
 *
 * Copyright (C) 2014-2015 OpenSearchServer Inc.
 * 
 * http://www.qwazr.com
 * 
 * This file is part of QWAZR.
 *
 * QWAZR is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * QWAZR is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with QWAZR. 
 *  If not, see <http://www.gnu.org/licenses/>.
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

	public final static String SERVICE_NAME = "webcrawler";

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
		ClusterServer.load(this, getCurrentDataDir(), null, null);
		ScriptManager.load(this, getCurrentDataDir());
		load(this);
	}

	public static void main(String[] args) throws IOException, ParseException,
			ServletException {
		new WebCrawlerServer().start(args);
		ClusterManager.INSTANCE.registerMe(SERVICE_NAME);
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

/*
 * Copyright 2017 Emmanuel Keller / QWAZR
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
package com.qwazr.crawler.web.test;

import com.google.common.io.Files;
import com.qwazr.utils.process.ProcessUtils;
import com.qwazr.webapps.WebappServer;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class WebAppTestServer {

	private static Process webAppProcess;

	public static String URL = "http://localhost:9190";

	public static synchronized void start() throws Exception {
		if (webAppProcess != null)
			return;
		final File dataDir = Files.createTempDir();
		FileUtils.copyDirectoryToDirectory(Paths.get("src", "test", "statics").toFile(), dataDir);
		Map<String, String> env = new HashMap<>();
		env.put("QWAZR_DATA", dataDir.getAbsolutePath());
		env.put("PUBLIC_ADDR", "localhost");
		env.put("LISTEN_ADDR", "localhost");
		env.put("WEBAPP_PORT", "9190");
		env.put("WEBSERVICE_PORT", "9191");
		env.put("QWAZR_ETC_DIR", new File("src/test/etc").getAbsolutePath());
		webAppProcess = ProcessUtils.java(WebappServer.class, env);
	}

	public static synchronized void stop() throws InterruptedException {
		if (webAppProcess == null)
			return;
		webAppProcess.destroy();
		webAppProcess.waitFor();
		webAppProcess = null;
	}
}

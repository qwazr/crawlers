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
package com.qwazr.crawler.web;

import com.qwazr.server.GenericServer;
import com.qwazr.server.GenericServerBuilder;
import com.qwazr.server.configuration.ServerConfiguration;
import com.qwazr.utils.WaitFor;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;

public class WebAppTestServer {

    private final static Logger LOGGER = Logger.getLogger(WebAppTestServer.class.getName());

    private static GenericServer server;

    public static String URL = "http://localhost:9190";

    public static synchronized void start() throws Exception {
        if (server != null)
            return;
        final Path dataDir = Files.createTempDirectory("test");
        FileUtils.copyDirectoryToDirectory(Paths.get("src", "test", "statics").toFile(), dataDir.toFile());
        final GenericServerBuilder serverBuilder = GenericServer.of(
                ServerConfiguration.of()
                        .data(dataDir)
                        .publicAddress("localhost")
                        .listenAddress("localhost")
                        .webAppPort(9190)
                        .webServicePort(9191)
                        .build());

        serverBuilder.getWebAppContext()
                .getWebappBuilder()
                .registerDefaultFaviconServlet()
                .registerStaticServlet("/*", dataDir.resolve("statics").resolve("html"));


        server = serverBuilder.build();
        server.start(true);
        Thread.sleep(1000);

        final URL url = new URL(URL);
        WaitFor.of().timeOut(TimeUnit.MINUTES, 1).until(() -> {
            try {
                return 200 == ((HttpURLConnection) url.openConnection()).getResponseCode();
            } catch (IOException e) {
                LOGGER.warning(() -> "Waiting for WebAppTestServer to start: " + e.getMessage());
                Thread.sleep(500);
                return false;
            }
        });
        LOGGER.info(() -> "WebAppTestServer started");
    }

    public static synchronized void stop() {
        if (server == null)
            return;
        server.close();
        server = null;
    }
}

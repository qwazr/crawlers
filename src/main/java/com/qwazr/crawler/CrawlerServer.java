/*
 * Copyright 2015-2020 Emmanuel Keller / QWAZR
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
package com.qwazr.crawler;

import com.qwazr.cluster.ClusterManager;
import com.qwazr.cluster.ClusterServiceInterface;
import com.qwazr.crawler.file.FileCrawlerManager;
import com.qwazr.crawler.file.FileCrawlerServiceBuilder;
import com.qwazr.crawler.file.FileCrawlerServiceInterface;
import com.qwazr.crawler.ftp.FtpCrawlerManager;
import com.qwazr.crawler.ftp.FtpCrawlerServiceBuilder;
import com.qwazr.crawler.web.WebCrawlerManager;
import com.qwazr.crawler.web.WebCrawlerServiceBuilder;
import com.qwazr.crawler.web.WebCrawlerServiceInterface;
import com.qwazr.library.LibraryManager;
import com.qwazr.library.LibraryServiceInterface;
import com.qwazr.scripts.ScriptManager;
import com.qwazr.server.ApplicationBuilder;
import com.qwazr.server.BaseServer;
import com.qwazr.server.GenericServer;
import com.qwazr.server.GenericServerBuilder;
import com.qwazr.server.RestApplication;
import com.qwazr.server.WelcomeShutdownService;
import com.qwazr.server.configuration.ServerConfiguration;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CrawlerServer implements BaseServer {

    private final GenericServer server;
    private final WebCrawlerServiceBuilder webCrawlerServiceBuilder;
    private final FileCrawlerServiceBuilder fileCrawlerServiceBuilder;
    private final FtpCrawlerServiceBuilder ftpCrawlerServiceBuilder;

    private CrawlerServer(final ServerConfiguration configuration) throws IOException {
        final ExecutorService executorService = Executors.newCachedThreadPool();
        final GenericServerBuilder builder = GenericServer.of(configuration, executorService);

        final Set<String> services = new HashSet<>();
        services.add(ClusterServiceInterface.SERVICE_NAME);
        services.add(LibraryServiceInterface.SERVICE_NAME);
        services.add(WebCrawlerServiceInterface.SERVICE_NAME);
        services.add(FileCrawlerServiceInterface.SERVICE_NAME);

        final ApplicationBuilder webServices = ApplicationBuilder.of("/*").classes(RestApplication.JSON_CLASSES).
                singletons(new WelcomeShutdownService());

        final ClusterManager clusterManager =
                new ClusterManager(executorService, configuration).registerProtocolListener(builder, services);
        webServices.singletons(clusterManager.getService());

        final LibraryManager libraryManager =
                new LibraryManager(configuration.dataDirectory, configuration.getEtcFiles());
        builder.shutdownListener(server -> libraryManager.close());
        webServices.singletons(libraryManager.getService());

        final ScriptManager scriptManager =
                new ScriptManager(executorService, clusterManager, libraryManager.getService(),
                        configuration.dataDirectory);
        webServices.singletons(scriptManager.getService());


        final Path webCrawlerDirectory = configuration.dataDirectory.resolve("webcrawler");
        if (!Files.exists(webCrawlerDirectory))
            Files.createDirectory(webCrawlerDirectory);

        final WebCrawlerManager webCrawlerManager = new WebCrawlerManager(
                webCrawlerDirectory, clusterManager, scriptManager, executorService);
        builder.shutdownListener(server -> webCrawlerManager.close());
        webServices.singletons(webCrawlerManager.getService());
        webCrawlerServiceBuilder = new WebCrawlerServiceBuilder(executorService, clusterManager, webCrawlerManager);

        final Path fileCrawlerDirectory = configuration.dataDirectory.resolve("filecrawler");
        if (!Files.exists(fileCrawlerDirectory))
            Files.createDirectory(fileCrawlerDirectory);

        final FileCrawlerManager fileCrawlerManager = new FileCrawlerManager(
                fileCrawlerDirectory, clusterManager, scriptManager, executorService);
        builder.shutdownListener(server -> fileCrawlerManager.close());
        webServices.singletons(fileCrawlerManager.getService());
        fileCrawlerServiceBuilder = new FileCrawlerServiceBuilder(clusterManager, fileCrawlerManager);

        final Path ftpCrawlerDirectory = configuration.dataDirectory.resolve("ftpcrawler");
        if (!Files.exists(ftpCrawlerDirectory))
            Files.createDirectory(ftpCrawlerDirectory);

        final FtpCrawlerManager ftpCrawlerManager = new FtpCrawlerManager(
                ftpCrawlerDirectory, clusterManager, scriptManager, executorService);
        builder.shutdownListener(server -> ftpCrawlerManager.close());
        webServices.singletons(ftpCrawlerManager.getService());
        ftpCrawlerServiceBuilder = new FtpCrawlerServiceBuilder(clusterManager, ftpCrawlerManager);

        builder.getWebServiceContext().jaxrs(webServices);

        server = builder.build();
    }

    public WebCrawlerServiceBuilder getWebCrawlerServiceBuilder() {
        return webCrawlerServiceBuilder;
    }

    public FileCrawlerServiceBuilder getFileCrawlerServiceBuilder() {
        return fileCrawlerServiceBuilder;
    }

    public FtpCrawlerServiceBuilder getFtpCrawlerServiceBuilder() {
        return ftpCrawlerServiceBuilder;
    }

    @Override
    public GenericServer getServer() {
        return server;
    }

    private static volatile CrawlerServer INSTANCE;

    public static CrawlerServer getInstance() {
        return INSTANCE;
    }

    public static synchronized void main(final String... args) throws Exception {
        shutdown();
        INSTANCE = new CrawlerServer(new ServerConfiguration(args));
        INSTANCE.start();
    }

    public static synchronized void shutdown() {
        if (INSTANCE == null)
            return;
        INSTANCE.stop();
        INSTANCE = null;
    }

}

/*
 * Copyright 2017-2020 Emmanuel Keller / QWAZR
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
package com.qwazr.crawler.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.qwazr.server.ServerException;
import com.qwazr.utils.ObjectMappers;
import com.qwazr.utils.WaitFor;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.core.Response;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;

public abstract class CrawlManager<
        MANAGER extends CrawlManager<MANAGER, THREAD, SESSION, DEFINITION, STATUS, ITEM>,
        THREAD extends CrawlThread<THREAD, DEFINITION, STATUS, MANAGER, SESSION, ITEM>,
        SESSION extends CrawlSessionBase<SESSION, THREAD, MANAGER, DEFINITION, STATUS, ITEM>,
        DEFINITION extends CrawlDefinition<DEFINITION>,
        STATUS extends CrawlStatus<STATUS>,
        ITEM extends CrawlItem
        > extends AttributesBase implements AutoCloseable {

    public final static String CRAWL_DB_NAME = "crawler.db";
    public final static String MAP_SESSION_NAME = "sessions";
    public final static String SESSION_DIRECTORY_NAME = "sessions";

    private final ConcurrentHashMap<String, THREAD> currentCrawlThreads;
    private final Class<STATUS> statusClass;
    private final HTreeMap<String, byte[]> crawlStatusMap;
    private final HTreeMap<String, byte[]> crawlDefinitionMap;

    protected final ExecutorService executorService;
    private final Logger logger;

    protected final String myAddress;
    private final DB database;
    protected final Path sessionsDirectory;

    protected CrawlManager(final Path crawlerRootDirectory,
                           final String myAddress,
                           final ExecutorService executorService,
                           final Logger logger,
                           final Class<STATUS> statusClass) throws IOException {
        this.database = DBMaker
                .fileDB(crawlerRootDirectory.resolve(CRAWL_DB_NAME).toFile())
                .transactionEnable()
                .make();
        this.sessionsDirectory = crawlerRootDirectory.resolve(SESSION_DIRECTORY_NAME);
        if (!Files.exists(sessionsDirectory))
            Files.createDirectory(sessionsDirectory);
        this.currentCrawlThreads = new ConcurrentHashMap<>();
        this.myAddress = myAddress;
        this.executorService = executorService;
        this.logger = logger;
        this.statusClass = statusClass;
        this.crawlStatusMap = database.hashMap(MAP_SESSION_NAME)
                .keySerializer(Serializer.STRING)
                .valueSerializer(Serializer.BYTE_ARRAY)
                .createOrOpen();
        this.crawlDefinitionMap = database.hashMap(MAP_SESSION_NAME)
                .keySerializer(Serializer.STRING)
                .valueSerializer(Serializer.BYTE_ARRAY)
                .createOrOpen();

    }

    public void forEachLiveSession(final BiConsumer<String, STATUS> consumer) {
        currentCrawlThreads.forEach((key, crawl) -> consumer.accept(key, crawl.getStatus()));
    }

    public STATUS getSessionStatus(final String sessionName) {
        final byte[] bytes = crawlStatusMap.get(sessionName);
        try {
            return bytes == null ? null : ObjectMappers.SMILE.readValue(bytes, statusClass);
        } catch (IOException e) {
            throw new ServerException(Response.Status.INTERNAL_SERVER_ERROR, "Error while reading the status of " + sessionName + " : " + e.getMessage(), e);
        }
    }

    void setSessionStatus(final String sessionName, STATUS status) {
        try {
            crawlStatusMap.put(sessionName, ObjectMappers.SMILE.writeValueAsBytes(status));
            database.commit();
        } catch (JsonProcessingException e) {
            throw new ServerException(Response.Status.INTERNAL_SERVER_ERROR, "Error while reading writing status of " + sessionName + " : " + e.getMessage(), e);
        }
    }


    public void abortSession(final String sessionName, final String abortingReason) {
        final THREAD crawlThread = currentCrawlThreads.get(sessionName);
        if (crawlThread == null)
            throw new ServerException(Response.Status.NOT_FOUND, "The crawl session was not running: " + sessionName);
        logger.info(() -> "Aborting crawl session: " + sessionName + " - " + abortingReason);
        crawlThread.abort(abortingReason);
    }

    protected abstract THREAD newCrawlThread(final String sessionName,
                                             final DEFINITION crawlDefinition);

    protected <COLLECTOR_FACTORY extends CrawlCollectorFactory<ITEM, DEFINITION>> COLLECTOR_FACTORY newCrawlCollectorFactory(final DEFINITION crawlDefinition,
                                                                                                                             final Class<? extends COLLECTOR_FACTORY> factoryClass) {
        if (crawlDefinition.crawlCollectorFactoryClass == null)
            return null;
        try {
            return factoryClass.cast(Class
                    .forName(crawlDefinition.crawlCollectorFactoryClass)
                    .getConstructor()
                    .newInstance());
        } catch (ReflectiveOperationException e) {
            throw new InternalServerErrorException("Can't create the factory: " + crawlDefinition.crawlCollectorFactoryClass, e);
        }
    }

    public STATUS runSession(final String sessionName,
                             final DEFINITION crawlDefinition) {

        currentCrawlThreads.compute(sessionName, (key, currentCrawl) -> {
            if (currentCrawl != null)
                throw new ServerException(Response.Status.CONFLICT, "The session already exists: " + sessionName);
            try {
                crawlDefinitionMap.put(sessionName, ObjectMappers.SMILE.writeValueAsBytes(crawlDefinition));
                database.commit();
            } catch (JsonProcessingException e) {
                throw new InternalServerErrorException("Can't read the crawl definition: " + e.getMessage(), e);
            }
            logger.info(() -> "Create crawl session: " + sessionName);
            final THREAD newCrawlThread = newCrawlThread(sessionName, crawlDefinition);
            newCrawlThread.start();
            return newCrawlThread;
        });
        return getSessionStatus(sessionName);
    }

    void removeSession(final String sessionName) {
        logger.info(() -> "Remove session: " + sessionName);
        currentCrawlThreads.remove(sessionName).abort("Session removed by the user");
    }

    @Override
    public void close() {
        currentCrawlThreads.forEach((name, thread) -> thread.session.close());
        try {
            WaitFor.of().timeOut(TimeUnit.HOURS, 1).pauseTime(TimeUnit.SECONDS, 1).until(currentCrawlThreads::isEmpty);
        } catch (final InterruptedException e) {
            logger.log(Level.SEVERE, e, () -> "Cannot stop the crawl session");
        }
        database.close();
    }

}

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
import com.qwazr.utils.StringUtils;
import com.qwazr.utils.WaitFor;
import com.qwazr.utils.WildcardMatcher;
import com.qwazr.utils.concurrent.ReadWriteLock;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.IntConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotAcceptableException;
import javax.ws.rs.NotFoundException;
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
        STATUS extends CrawlSessionStatus<STATUS>,
        ITEM extends CrawlItem<?>
        > implements Attributes, AutoCloseable {

    public final static String CRAWL_DB_NAME = "crawler.db";
    public final static String MAP_SESSION_STATUS_NAME = "status";
    public final static String MAP_SESSION_DEFINITION_NAME = "definition";
    public final static String SESSION_DIRECTORY_NAME = "sessions";

    private final ConcurrentHashMap<String, THREAD> liveCrawlThreads;
    private final Class<STATUS> statusClass;
    private final Class<DEFINITION> definitionClass;
    private final HTreeMap<String, byte[]> crawlStatusMap;
    private final HTreeMap<String, byte[]> crawlDefinitionMap;
    private final ReadWriteLock mapLock;

    private final ExecutorService sessionExecutorService;
    protected final ExecutorService crawlExecutorService;
    private final Logger logger;

    protected final String myAddress;
    private final DB database;
    protected final Path sessionsDirectory;

    private final Map<String, Object> attributes;

    protected CrawlManager(final Path crawlerRootDirectory,
                           final String myAddress,
                           final ExecutorService sessionExecutorService,
                           final ExecutorService crawlExecutorService,
                           final Logger logger,
                           final Class<STATUS> statusClass,
                           final Class<DEFINITION> definitionClass) throws IOException {
        this.database = DBMaker
                .fileDB(crawlerRootDirectory.resolve(CRAWL_DB_NAME).toFile())
                .transactionEnable()
                .make();
        this.sessionsDirectory = crawlerRootDirectory.resolve(SESSION_DIRECTORY_NAME);
        if (!Files.exists(sessionsDirectory))
            Files.createDirectory(sessionsDirectory);
        this.liveCrawlThreads = new ConcurrentHashMap<>();
        this.myAddress = myAddress;
        this.sessionExecutorService = sessionExecutorService;
        this.crawlExecutorService = crawlExecutorService;
        this.logger = logger;
        this.statusClass = statusClass;
        this.definitionClass = definitionClass;
        this.mapLock = ReadWriteLock.reentrant(true);
        this.crawlStatusMap = database.hashMap(MAP_SESSION_STATUS_NAME)
                .keySerializer(Serializer.STRING)
                .valueSerializer(Serializer.BYTE_ARRAY)
                .createOrOpen();
        this.crawlDefinitionMap = database.hashMap(MAP_SESSION_DEFINITION_NAME)
                .keySerializer(Serializer.STRING)
                .valueSerializer(Serializer.BYTE_ARRAY)
                .createOrOpen();
        this.attributes = new ConcurrentHashMap<>();
    }

    public void registerAttribute(final String name, final Object instance) {
        attributes.put(name, instance);
    }

    @Override
    public <T> T getInstance(final String name, final Class<T> instanceClass) {
        return instanceClass.cast(attributes.get(name));
    }

    public LinkedHashMap<String, STATUS> getSessions(final String wildcardPattern,
                                                     final int start,
                                                     final int rows,
                                                     final IntConsumer totalNumber) {
        return mapLock.read(() -> {
            final LinkedHashMap<String, STATUS> sessionStatuses = new LinkedHashMap<>();
            int t = 0;
            int s = start;
            final Iterator<String> sessionNamesIterator = crawlStatusMap.getKeys().iterator();
            while (s != 0 && sessionNamesIterator.hasNext()) {
                sessionNamesIterator.next();
                s--;
            }
            final WildcardMatcher wildcardMatcher = StringUtils.isBlank(wildcardPattern) ? null : new WildcardMatcher(wildcardPattern);
            int r = rows;
            while (r != 0 && sessionNamesIterator.hasNext()) {
                final String sessionName = sessionNamesIterator.next();
                if (wildcardMatcher == null || wildcardMatcher.match(sessionName)) {
                    if (r > 0) {
                        sessionStatuses.put(sessionName, readSessionStatus(sessionName));
                        r--;
                    }
                    t++;
                }
            }
            totalNumber.accept(t);
            return sessionStatuses;
        });
    }

    private STATUS readSessionStatus(final String sessionName) {
        final byte[] bytes = crawlStatusMap.get(sessionName);
        try {
            return bytes == null ? null : ObjectMappers.SMILE.readValue(bytes, statusClass);
        } catch (IOException e) {
            throw new InternalServerErrorException(
                    "Error while reading the status of " + sessionName + " : " + e.getMessage(), e);
        }
    }

    public STATUS getSessionStatus(final String sessionName) {
        return mapLock.read(() -> readSessionStatus(sessionName));
    }

    public DEFINITION getSessionDefinition(final String sessionName) {
        return mapLock.read(() -> {
            final byte[] bytes = crawlDefinitionMap.get(sessionName);
            try {
                return bytes == null ? null : ObjectMappers.SMILE.readValue(bytes, definitionClass);
            } catch (IOException e) {
                throw new InternalServerErrorException(
                        "Error while reading the definition of " + sessionName + " : " + e.getMessage(), e);
            }
        });
    }

    void setSessionStatus(final String sessionName, final STATUS status) {
        mapLock.read(() -> {
            try {
                crawlStatusMap.put(sessionName, ObjectMappers.SMILE.writeValueAsBytes(status));
                database.commit();
            } catch (JsonProcessingException e) {
                throw new InternalServerErrorException(
                        "Error while reading writing status of " + sessionName + " : " + e.getMessage(), e);
            }
        });
    }


    public void abortSession(final String sessionName, final String abortingReason) {
        mapLock.read(() -> {
            final THREAD crawlThread = liveCrawlThreads.get(sessionName);
            if (crawlThread == null)
                throw new NotFoundException("The crawl session was not running: " + sessionName);
            logger.info(() -> "Aborting crawl session: " + sessionName + " - " + abortingReason);
            crawlThread.abort(abortingReason);
        });
    }

    protected abstract THREAD newCrawlThread(final String sessionName,
                                             final DEFINITION crawlDefinition);

    protected <COLLECTOR_FACTORY extends CrawlCollectorFactory<ITEM, DEFINITION>>
    CrawlCollector<ITEM> newCrawlCollector(final DEFINITION crawlDefinition,
                                           final Class<? extends COLLECTOR_FACTORY> factoryClass) {
        if (crawlDefinition.crawlCollectorFactoryClass == null)
            return null;
        try {
            COLLECTOR_FACTORY factory = factoryClass.cast(Class
                    .forName(crawlDefinition.crawlCollectorFactoryClass)
                    .getConstructor()
                    .newInstance());
            return factory.createCrawlCollector(this, crawlDefinition);
        } catch (ReflectiveOperationException e) {
            throw new InternalServerErrorException("Can't create the factory: " + crawlDefinition.crawlCollectorFactoryClass, e);
        }
    }

    public STATUS runSession(final String sessionName,
                             final DEFINITION crawlDefinition) {

        return mapLock.write(() -> {
            liveCrawlThreads.compute(sessionName, (key, currentCrawl) -> {
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
                CompletableFuture.runAsync(newCrawlThread, sessionExecutorService).whenComplete((r, e) -> {
                    liveCrawlThreads.remove(sessionName);
                    if (e != null)
                        logger.log(Level.SEVERE, e,
                                () -> "Error on crawl session " + sessionName + ": " + e.getMessage());
                });
                return newCrawlThread;
            });
            return getSessionStatus(sessionName);
        });
    }

    void removeSession(final String sessionName) {
        mapLock.write(() -> {
            if (liveCrawlThreads.containsKey(sessionName))
                throw new NotAcceptableException("The session is currently running.");
            crawlStatusMap.remove(sessionName);
            crawlDefinitionMap.remove(sessionName);
            database.commit();
            try {
                Files.deleteIfExists(sessionsDirectory.resolve(sessionName));
            } catch (IOException e) {
                throw new InternalServerErrorException("Error while removing session: " + sessionName, e);
            }
        });
    }

    @Override
    public void close() {
        mapLock.write(() -> {
            liveCrawlThreads.forEach((name, thread) -> thread.session.abort(null));
            try {
                WaitFor.of()
                        .timeOut(TimeUnit.HOURS, 1)
                        .pauseTime(TimeUnit.SECONDS, 1)
                        .until(liveCrawlThreads::isEmpty);
            } catch (final InterruptedException e) {
                logger.log(Level.SEVERE, e, () -> "Cannot stop the crawl session");
            }
            database.close();
        });
    }

}

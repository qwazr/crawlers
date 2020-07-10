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
package com.qwazr.crawler.file;

import com.qwazr.crawler.common.CrawlThread;
import com.qwazr.utils.StringUtils;
import java.io.File;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileCrawlThread extends CrawlThread
        <FileCrawlThread, FileCrawlDefinition, FileCrawlStatus,
                FileCrawlerManager, FileCrawlSession, FileCrawlItem>
        implements FileVisitor<Path> {

    private final FileCrawlDefinition crawlDefinition;
    private final Path startPath;

    FileCrawlThread(FileCrawlerManager manager, FileCrawlSession session, Logger logger) {
        super(manager, session, logger);
        this.crawlDefinition = session.getCrawlDefinition();
        this.startPath = Paths.get(crawlDefinition.getEntryPath());
    }

    private int computeDepth(final Path path) {
        int depth = 0;
        Path p = path;
        while (p != null && !p.equals(startPath)) {
            p = p.getParent();
            depth++;
        }
        return depth;
    }

    @Override
    protected void runner() throws Exception {
        if (!Files.exists(startPath)) {
            logger.warning(() -> "The path does not exists: " + startPath.toAbsolutePath());
            return;
        }
        Files.walkFileTree(startPath, Collections.emptySet(),
                crawlDefinition.maxDepth == null ? Integer.MAX_VALUE : crawlDefinition.maxDepth, this);
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
        if (session.isAborting())
            return FileVisitResult.TERMINATE;
        final FileCrawlItem.Builder builder = new FileCrawlItem.Builder(computeDepth(dir), dir, attrs);
        final FileCrawlItem current = crawl(builder);
        if (current.isIgnored())
            return FileVisitResult.SKIP_SUBTREE;
        return FileVisitResult.CONTINUE;
    }

    private FileCrawlItem crawl(final FileCrawlItem.Builder builder) {
        final String currentPathString = builder.attributes.isDirectory() ?
                StringUtils.ensureSuffix(builder.path.toString(), File.separator) :
                builder.path.toString();
        checkPassInclusionExclusion(builder, currentPathString);
        final FileCrawlItem current = builder.build();
        try {
            session.collect(current);
            return current;
        } catch (Exception e) {
            final String err = "File crawling error on " + currentPathString;
            logger.log(Level.WARNING, err, e);
            session.incErrorCount(err + ": " + ExceptionUtils.getRootCauseMessage(e));
            return current;
        }
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
        if (session.isAborting())
            return FileVisitResult.TERMINATE;
        if (crawlDefinition.crawlWaitMs != null)
            session.sleep(crawlDefinition.crawlWaitMs);
        crawl(new FileCrawlItem.Builder(computeDepth(file), file, attrs));
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException e) {
        final String error = "File crawling error on " + file;
        logger.log(Level.WARNING, error, e);
        session.incErrorCount(error);
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException e) {
        if (e != null)
            logger.log(Level.WARNING, e, () -> "Directory crawling error on " + dir);
        return FileVisitResult.CONTINUE;
    }
}

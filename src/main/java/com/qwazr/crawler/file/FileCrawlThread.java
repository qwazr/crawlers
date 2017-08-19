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
package com.qwazr.crawler.file;

import com.qwazr.crawler.common.CrawlSessionImpl;
import com.qwazr.crawler.common.CrawlThread;
import com.qwazr.crawler.common.EventEnum;
import com.qwazr.utils.ExceptionUtils;

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

public class FileCrawlThread extends CrawlThread<FileCrawlerManager> implements FileVisitor<Path> {

	private final FileCrawlDefinition crawlDefinition;
	private int currentDepth;

	public FileCrawlThread(FileCrawlerManager manager, CrawlSessionImpl<FileCrawlDefinition> session, Logger logger) {
		super(manager, session, logger);
		this.crawlDefinition = session.getCrawlDefinition();
	}

	@Override
	protected void runner() throws Exception {
		script(EventEnum.before_session, null);
		try {
			final Path startPath = Paths.get(crawlDefinition.getEntryPath());
			if (!Files.exists(startPath)) {
				logger.warning(() -> "The path does not exists: " + startPath.toAbsolutePath());
				return;
			}
			Files.walkFileTree(startPath, Collections.emptySet(),
					crawlDefinition.maxDepth == null ? Integer.MAX_VALUE : crawlDefinition.maxDepth, this);
		} finally {
			script(EventEnum.after_session, null);
		}
	}

	@Override
	public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
		if (session.isAborting())
			return FileVisitResult.TERMINATE;
		currentDepth++;
		if (crawlDefinition.maxDepth != null && currentDepth >= crawlDefinition.maxDepth)
			return FileVisitResult.SKIP_SUBTREE;
		final CurrentPath currentPath = new CurrentPath(dir, attrs, currentDepth);
		crawl(currentPath);
		if (currentPath.isIgnored())
			return FileVisitResult.SKIP_SUBTREE;
		return FileVisitResult.CONTINUE;
	}

	private void crawl(final CurrentPath current) {
		try {

			checkPassInclusionExclusion(current, current.pathString);
			script(EventEnum.before_crawl, current);

			if (current.isIgnored())
				session.incIgnoredCount();
			else {
				current.setCrawled();
				session.incCrawledCount();
			}
			script(EventEnum.after_crawl, current);
		} catch (Exception e) {
			final String err = "File crawling error on " + current.pathString;
			logger.log(Level.WARNING, err, e);
			session.incErrorCount(err + ": " + ExceptionUtils.getRootCauseMessage(e));
		}
	}

	@Override
	public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
		if (session.isAborting())
			return FileVisitResult.TERMINATE;
		if (crawlDefinition.crawlWaitMs != null)
			session.sleep(crawlDefinition.crawlWaitMs);
		crawl(new CurrentPath(file, attrs, currentDepth));
		return FileVisitResult.CONTINUE;
	}

	@Override
	public FileVisitResult visitFileFailed(Path file, IOException e) throws IOException {
		final String error = "File crawling error on " + file;
		logger.log(Level.WARNING, error, e);
		session.incErrorCount(error);
		return FileVisitResult.CONTINUE;
	}

	@Override
	public FileVisitResult postVisitDirectory(Path dir, IOException e) throws IOException {
		if (e != null)
			logger.log(Level.WARNING, e, () -> "Directory crawling error on " + dir);
		else
			currentDepth--;
		return FileVisitResult.CONTINUE;
	}
}

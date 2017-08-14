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
 **/
package com.qwazr.crawler.file;

import com.qwazr.crawler.common.CrawlSessionImpl;
import com.qwazr.crawler.common.CrawlThread;
import com.qwazr.crawler.common.EventEnum;
import com.qwazr.utils.WildcardMatcher;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileCrawlThread extends CrawlThread<FileCrawlerManager> implements FileVisitor<Path> {

	private final FileCrawlDefinition crawlDefinition;
	private final List<WildcardMatcher> inclusionMatcherList;
	private final List<WildcardMatcher> exclusionMatcherList;
	private int currentDepth;

	public FileCrawlThread(FileCrawlerManager manager, CrawlSessionImpl<FileCrawlDefinition> session, Logger logger) {
		super(manager, session, logger);
		this.crawlDefinition = session.getCrawlDefinition();
		this.inclusionMatcherList = WildcardMatcher.getList(crawlDefinition.inclusionPatterns);
		this.exclusionMatcherList = WildcardMatcher.getList(crawlDefinition.exclusionPatterns);
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

	private boolean checkPattern(Path path) {
		final String text = path.toString();
		final Boolean inc = matches(text, inclusionMatcherList, null);
		if (inc != null && !inc)
			return false;
		final Boolean exc = matches(text, exclusionMatcherList, false);
		return exc == null || !exc;
	}

	@Override
	public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
		if (session.isAborting())
			return FileVisitResult.TERMINATE;
		currentDepth++;
		if (crawlDefinition.maxDepth != null && currentDepth >= crawlDefinition.maxDepth)
			return FileVisitResult.SKIP_SUBTREE;
		return FileVisitResult.CONTINUE;
	}

	@Override
	public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
		if (session.isAborting())
			return FileVisitResult.TERMINATE;
		if (!checkPattern(file)) {
			session.incIgnoredCount();
			return FileVisitResult.CONTINUE;
		}
		final Consumer<Map<String, Object>> attributesProvider = attributes -> {
			attributes.put("path", file);
			attributes.put("attrs", attrs);
		};
		try {
			session.setCurrentCrawl(file.toString(), currentDepth);
			script(EventEnum.before_crawl, attributesProvider);
			session.incCrawledCount();
		} catch (Exception e) {
			final String error = "File crawling error on " + file;
			logger.log(Level.WARNING, error, e);
			session.incErrorCount(error);
		}
		if (crawlDefinition.crawlWaitMs != null)
			session.sleep(crawlDefinition.crawlWaitMs);
		script(EventEnum.after_crawl, attributesProvider);
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

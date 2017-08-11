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
import com.qwazr.crawler.common.CrawlStatus;
import com.qwazr.crawler.common.CrawlThread;
import com.qwazr.crawler.common.EventEnum;
import com.qwazr.utils.WildcardMatcher;
import com.qwazr.utils.concurrent.ThreadUtils;

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
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileCrawlThread extends CrawlThread<FileCrawlerManager> implements FileVisitor<Path> {

	private final FileCrawlDefinition crawlDefinition;
	private final List<WildcardMatcher> exclusionMatcherList;

	public FileCrawlThread(FileCrawlerManager manager, CrawlSessionImpl<FileCrawlDefinition> session, Logger logger) {
		super(manager, session, logger);
		this.crawlDefinition = session.getCrawlDefinition();
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

	@Override
	public CrawlStatus getStatus() {
		return new CrawlStatus(manager.getMyAddress(), crawlDefinition.entryPath, session);
	}

	@Override
	public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
		if (session.isAborting())
			return FileVisitResult.TERMINATE;
		if (exclusionMatcherList != null && WildcardMatcher.anyMatch(dir.toString(), exclusionMatcherList))
			return FileVisitResult.SKIP_SUBTREE;
		return FileVisitResult.CONTINUE;
	}

	@Override
	public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
		if (session.isAborting())
			return FileVisitResult.TERMINATE;
		if (exclusionMatcherList != null && WildcardMatcher.anyMatch(file.toString(), exclusionMatcherList))
			return FileVisitResult.CONTINUE;
		final Consumer<Map<String, Object>> attributesProvider = attributes -> {
			attributes.put("path", file);
			attributes.put("attrs", attrs);
		};
		try {
			script(EventEnum.before_crawl, attributesProvider);
			session.incCrawledCount();
		} catch (Exception e) {
			logger.log(Level.WARNING, e, () -> "File crawling error on " + file.toAbsolutePath());
			session.incErrorCount();
		}
		if (crawlDefinition.crawlWaitMs != null)
			ThreadUtils.sleep(crawlDefinition.crawlWaitMs, TimeUnit.MILLISECONDS);
		script(EventEnum.after_crawl, attributesProvider);
		return FileVisitResult.CONTINUE;
	}

	@Override
	public FileVisitResult visitFileFailed(Path file, IOException e) throws IOException {
		logger.log(Level.WARNING, e, () -> "File crawling error on " + file);
		session.incErrorCount();
		return FileVisitResult.CONTINUE;
	}

	@Override
	public FileVisitResult postVisitDirectory(Path dir, IOException e) throws IOException {
		if (e != null)
			logger.log(Level.WARNING, e, () -> "Directory crawling error on " + dir);
		return FileVisitResult.CONTINUE;
	}
}

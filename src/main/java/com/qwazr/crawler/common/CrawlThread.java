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
package com.qwazr.crawler.common;

import com.qwazr.scripts.ScriptRunThread;
import com.qwazr.server.ServerException;
import com.qwazr.utils.TimeTracker;
import com.qwazr.utils.WildcardMatcher;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class CrawlThread<M extends CrawlManager> implements Runnable {

	private final CrawlDefinition crawlDefinition;
	private final TimeTracker timeTracker;
	protected final M manager;
	protected final CrawlSessionImpl session;
	protected final Logger logger;
	private final List<WildcardMatcher> exclusionMatcherList;
	private final List<WildcardMatcher> inclusionMatcherList;
	private final Map<String, Object> scriptGlobalObjects;

	protected CrawlThread(final M manager, final CrawlSessionImpl session, final Logger logger) {
		this.manager = manager;
		this.session = session;
		this.crawlDefinition = session.getCrawlDefinition();
		this.timeTracker = session.getTimeTracker();
		this.logger = logger;
		this.exclusionMatcherList = WildcardMatcher.getList(crawlDefinition.exclusionPatterns);
		this.inclusionMatcherList = WildcardMatcher.getList(crawlDefinition.inclusionPatterns);
		this.scriptGlobalObjects = new HashMap<>();
	}

	public String getSessionName() {
		return session.getName();
	}

	protected void registerScriptGlobalObject(String key, Object object) {
		scriptGlobalObjects.put(key, object);
	}

	protected abstract void runner() throws Exception;

	/**
	 * Execute the scripts related to the passed event.
	 *
	 * @param event        the expected event
	 * @param currentCrawl the current crawl item
	 * @throws ServerException if the execution of the scripts failed
	 */
	protected void script(final EventEnum event, final CurrentCrawl currentCrawl) {
		if (crawlDefinition.scripts == null)
			return;
		final ScriptDefinition script = crawlDefinition.scripts.get(event);
		if (script == null)
			return;
		timeTracker.next(null);
		try {
			final Map<String, Object> attributes = new HashMap<>(scriptGlobalObjects);
			attributes.put("session", session);
			if (currentCrawl != null)
				attributes.put("current", currentCrawl);
			if (script.variables != null)
				attributes.putAll(script.variables);
			final ScriptRunThread scriptRunThread;
			try {
				scriptRunThread = manager.scriptManager.runSync(script.name, attributes);
			} catch (IOException | ClassNotFoundException e) {
				throw new ServerException(e);
			}
			if (scriptRunThread.getException() != null)
				throw new ServerException(scriptRunThread.getException());
		} finally {
			timeTracker.next("Event: " + event.name());
		}
	}

	/**
	 * Check the matching list. Returns the default value if the matching list is empty.
	 *
	 * @param text the text to check
	 * @return true if a pattern matched the text
	 */
	protected static Boolean matches(String text, List<WildcardMatcher> matchers, Boolean defaultValue) {
		if (matchers == null || matchers.isEmpty())
			return defaultValue;
		return WildcardMatcher.anyMatch(text, matchers);
	}

	protected boolean checkPassInclusionExclusion(String itemText, Consumer<Boolean> inclusionConsumer,
			Consumer<Boolean> exclusionConsumer) {

		// We check the inclusion/exclusion.
		final Boolean inInclusion = matches(itemText, inclusionMatcherList, null);
		final Boolean inExclusion = matches(itemText, exclusionMatcherList, null);
		if (inclusionConsumer != null)
			inclusionConsumer.accept(inInclusion);
		if (exclusionConsumer != null)
			exclusionConsumer.accept(inExclusion);

		return (inInclusion == null || inInclusion) && (inExclusion == null || !inExclusion);
	}

	protected void checkPassInclusionExclusion(CurrentCrawlImpl current, String itemText) {
		if (!checkPassInclusionExclusion(itemText, current::setInInclusion, current::setInExclusion))
			current.setIgnored();
	}

	@Override
	final public void run() {
		try {
			runner();
		} catch (Exception e) {
			logger.log(Level.SEVERE, e, e::getMessage);
			session.error(e);
		} finally {
			session.done();
			manager.removeSession(this);
		}
	}

	final public CrawlStatus getStatus() {
		return session.getCrawlStatus();
	}

	public void abort(final String abortingReason) {
		session.abort(abortingReason);
	}

}

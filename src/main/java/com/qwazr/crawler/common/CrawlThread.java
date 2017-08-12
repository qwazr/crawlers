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

import java.io.IOException;
import java.util.HashMap;
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

	protected CrawlThread(final M manager, final CrawlSessionImpl session, final Logger logger) {
		this.manager = manager;
		this.session = session;
		this.crawlDefinition = session.getCrawlDefinition();
		this.timeTracker = session.getTimeTracker();
		this.logger = logger;
	}

	public String getSessionName() {
		return session.getName();
	}

	protected abstract void runner() throws Exception;

	/**
	 * Execute the scripts related to the passed event.
	 *
	 * @param event the expected event
	 * @param attrs the optional attributes provider
	 * @return true if the scripts was executed, false if no scripts is attached
	 * to the event
	 * @throws ServerException        if the execution of the scripts failed
	 * @throws IOException            if any I/O exception occurs
	 * @throws ClassNotFoundException if the JAVA class is not found
	 */
	protected void script(EventEnum event, Consumer<Map<String, Object>> attrs) {
		if (crawlDefinition.scripts == null)
			return;
		final ScriptDefinition script = crawlDefinition.scripts.get(event);
		if (script == null)
			return;
		timeTracker.next(null);
		try {
			final Map<String, Object> attributes = new HashMap<>();
			attributes.put("session", session);
			if (script.variables != null)
				attributes.putAll(script.variables);
			if (attrs != null)
				attrs.accept(attributes);
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

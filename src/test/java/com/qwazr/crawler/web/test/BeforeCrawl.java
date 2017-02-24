/**
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
package com.qwazr.crawler.web.test;

import com.qwazr.scripts.ScriptInterface;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class BeforeCrawl implements ScriptInterface {

	public final static AtomicInteger count = new AtomicInteger();

	@Override
	public void run(final Map<String, ?> map) throws Exception {
		count.incrementAndGet();
	}
}
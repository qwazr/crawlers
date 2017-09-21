/*
 * Copyright 2014-2016 Emmanuel Keller / QWAZR
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
package com.qwazr.crawler.web;

import com.qwazr.crawler.common.CurrentCrawlImpl;

import java.net.URI;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

final class CurrentURIImpl extends CurrentCrawlImpl implements CurrentURI {

	private final URI uri;
	private final URI redirect;
	private final Map<URI, AtomicInteger> links;
	private final Boolean isRobotsTxtDisallow;

	CurrentURIImpl(Builder builder) {
		super(builder);
		this.uri = builder.uri;
		this.redirect = builder.redirect;
		this.isRobotsTxtDisallow = builder.isRobotsTxtDisallow;
		this.links = builder.links == null ? Collections.emptyMap() : Collections.unmodifiableMap(builder.links);
	}

	@Override
	public URI getUri() {
		return uri;
	}

	@Override
	public URI getRedirect() {
		return redirect;
	}

	@Override
	public Boolean isRobotsTxtDisallow() {
		return isRobotsTxtDisallow;
	}

	@Override
	public Map<URI, AtomicInteger> getLinks() {
		return links;
	}

	final static class Builder extends BaseBuilder<Builder> {

		final URI uri;
		final String uriString;
		private URI redirect;
		private Boolean isRobotsTxtDisallow;
		private LinkedHashMap<URI, AtomicInteger> links;

		protected Builder(URI uri, int depth) {
			super(Builder.class, depth);
			this.uri = uri;
			this.uriString = uri.toString();
		}

		public Builder redirect(URI redirect) {
			this.redirect = redirect;
			return this;
		}

		public Builder robotsTxtDisallow(Boolean isRobotsTxtDisallow) {
			this.isRobotsTxtDisallow = isRobotsTxtDisallow;
			return this;
		}

		public Builder link(URI uri) {
			if (uri == null)
				return this;
			if (links == null)
				links = new LinkedHashMap<>();
			links.computeIfAbsent(uri, u -> new AtomicInteger()).incrementAndGet();
			return this;
		}

		CurrentURIImpl build() {
			return new CurrentURIImpl(this);
		}

	}
}

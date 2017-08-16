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
import com.qwazr.crawler.web.driver.BrowserDriver;
import com.qwazr.utils.LinkUtils;
import org.apache.commons.lang3.StringUtils;

import java.net.URI;
import java.util.Collection;

class CurrentURIImpl extends CurrentCrawlImpl implements CurrentURI {

	final private URI initialURI;
	private volatile URI finalURI;
	private volatile URI baseURI;

	private volatile boolean isRedirected = false;
	private volatile boolean isStartDomain = false;
	private volatile boolean isStartSubDomain = false;
	private volatile boolean isRobotsTxtDisallow = false;

	private volatile Collection<URI> sameLevelLinks = null;
	private volatile Collection<URI> nextLevelLinks = null;
	private volatile Collection<URI> filteredLinks = null;

	CurrentURIImpl(URI uri, Integer depth) {
		super(depth);
		this.initialURI = uri;
	}

	@Override
	public URI getInitialURI() {
		return initialURI;
	}

	@Override
	public URI getUri() {
		return getURI();
	}

	@Override
	public URI getURI() {
		return finalURI != null ? finalURI : initialURI;
	}

	void setBaseURI(URI uri) {
		baseURI = uri;
	}

	void setFinalURI(URI uri) {
		finalURI = uri;
		if (finalURI != null)
			isRedirected = !finalURI.equals(initialURI);
	}

	void setRobotsTxtDisallow(boolean disallow) {
		this.isRobotsTxtDisallow = disallow;
	}

	void setCrawled() {
		super.setCrawled(true);
	}

	@Override
	public boolean isRobotsTxtDisallow() {
		return isRobotsTxtDisallow;
	}

	@Override
	public boolean isRedirected() {
		return isRedirected;
	}

	void setError(BrowserDriver driver, Exception e) {
		if (e == null) {
			setError((String) null);
			return;
		}
		String err = driver == null ? e.getMessage() : driver.getErrorMessage(e);
		if (StringUtils.isBlank(err))
			err = e.toString();
		if (StringUtils.isBlank(err))
			err = e.getClass().getName();
		setError(err);
	}

	@Override
	public void setSameLevelLinks(Collection<URI> links) {
		this.sameLevelLinks = links;
	}

	@Override
	public Collection<URI> getSameLevelLinks() {
		return sameLevelLinks;
	}

	@Override
	public void setLinks(Collection<URI> links) {
		this.nextLevelLinks = links;
	}

	@Override
	public Collection<URI> getLinks() {
		return nextLevelLinks;
	}

	@Override
	public void setFilteredLinks(Collection<URI> filteredLinks) {
		this.filteredLinks = filteredLinks;
	}

	@Override
	public Collection<URI> getFilteredLinks() {
		return filteredLinks;
	}

	void setStartDomain(boolean isStartDomain) {
		this.isStartDomain = isStartDomain;
	}

	@Override
	public boolean isStartDomain() {
		return isStartDomain;
	}

	void setStartSubDomain(boolean isStartSubDomain) {
		this.isStartSubDomain = isStartSubDomain;
	}

	@Override
	public boolean isStartSubDomain() {
		return isStartSubDomain;
	}

	@Override
	public void hrefToURICollection(Collection<String> hrefCollection, Collection<URI> uriCollection) {
		if (hrefCollection == null)
			return;
		URI uri = baseURI != null ? baseURI : getURI();
		for (String href : hrefCollection) {
			href = StringUtils.replace(href, " ", "%20");
			URI resolvedURI = LinkUtils.resolveQuietly(uri, href);
			if (resolvedURI != null)
				uriCollection.add(resolvedURI);
		}
	}

}

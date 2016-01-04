/**
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
 **/
package com.qwazr.crawler.web.manager;

import com.qwazr.utils.LinkUtils;
import org.apache.commons.lang3.StringUtils;

import java.net.URI;
import java.util.Collection;

public class CurrentURI {

	final private Integer depth;

	final private URI initialURI;
	private volatile URI finalURI;
	private volatile URI baseURI;

	private volatile boolean isIgnored = false;
	private volatile boolean isCrawled = false;
	private volatile boolean isRedirected = false;
	private volatile Boolean isInInclusion = null;
	private volatile Boolean isInExclusion = null;
	private volatile boolean isStartDomain = false;
	private volatile boolean isStartSubDomain = false;

	private String error = null;

	private volatile Collection<URI> sameLevelLinks = null;
	private volatile Collection<URI> nextLevelLinks = null;
	private volatile Collection<URI> filteredLinks = null;

	CurrentURI(URI uri, Integer depth) {
		this.initialURI = uri;
		this.depth = depth;
	}

	public URI getInitialURI() {
		return initialURI;
	}

	public URI getUri() {
		return getURI();
	}

	public URI getURI() {
		return finalURI != null ? finalURI : initialURI;
	}

	public Integer getDepth() {
		return depth;
	}

	void setBaseURI(URI uri) {
		baseURI = uri;
	}

	void setFinalURI(URI uri) {
		finalURI = uri;
		if (finalURI != null)
			isRedirected = !finalURI.equals(initialURI);
	}

	public void setInInclusion(Boolean isInInclusion) {
		this.isInInclusion = isInInclusion;
	}

	public Boolean isInInclusion() {
		return isInInclusion;
	}

	public void setInExclusion(Boolean isInExclusion) {
		this.isInExclusion = isInExclusion;
	}

	public Boolean isInExclusion() {
		return isInExclusion;
	}

	public void setIgnored(boolean ignored) {
		isIgnored = ignored;
	}

	public boolean isIgnored() {
		return isIgnored;
	}

	void setCrawled() {
		isCrawled = true;
	}

	public boolean isCrawled() {
		return isCrawled;
	}

	public boolean isRedirected() {
		return isRedirected;
	}

	void setError(Exception e) {
		error = e == null ? null : e.getMessage();
	}

	public String getError() {
		return error;
	}

	public void setSameLevelLinks(Collection<URI> links) {
		this.sameLevelLinks = links;
	}

	public Collection<URI> getSameLevelLinks() {
		return sameLevelLinks;
	}

	public void setLinks(Collection<URI> links) {
		this.nextLevelLinks = links;
	}

	public Collection<URI> getLinks() {
		return nextLevelLinks;
	}

	public void setFilteredLinks(Collection<URI> filteredLinks) {
		this.filteredLinks = filteredLinks;
	}

	public Collection<URI> getFilteredLinks() {
		return filteredLinks;
	}

	void setStartDomain(boolean isStartDomain) {
		this.isStartDomain = isStartDomain;
	}

	public boolean isStartDomain() {
		return isStartDomain;
	}

	void setStartSubDomain(boolean isStartSubDomain) {
		this.isStartSubDomain = isStartSubDomain;
	}

	public boolean isStartSubDomain() {
		return isStartSubDomain;
	}

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

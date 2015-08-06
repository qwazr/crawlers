/**
 * Copyright 2014-2015 Emmanuel Keller / QWAZR
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
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

	private volatile boolean isIgnored = false;
	private volatile boolean isCrawled = false;
	private volatile boolean isRedirected = false;
	private volatile boolean isInInclusion = false;
	private volatile boolean isInExclusion = false;
	private volatile boolean isStartDomain = false;
	private volatile boolean isStartSubDomain = false;

	private String error = null;

	private volatile Collection<URI> links = null;

	CurrentURI(URI uri, Integer depth) {
		this.initialURI = uri;
		this.depth = depth;
	}

	public URI getInitialURI() {
		return initialURI;
	}

	public URI getURI() {
		return finalURI != null ? finalURI : initialURI;
	}

	public Integer getDepth() {
		return depth;
	}

	void setFinalURI(URI uri) {
		finalURI = uri;
		if (finalURI != null)
			isRedirected = !finalURI.equals(initialURI);
	}

	public void setInInclusion(boolean isInInclusion) {
		this.isInInclusion = isInInclusion;
	}

	public boolean isInInclusion() {
		return isInInclusion;
	}

	public void setInExclusion(boolean isInExclusion) {
		this.isInExclusion = isInExclusion;
	}

	public boolean isInExclusion() {
		return isInExclusion;
	}

	public void setIgnored() {
		isIgnored = true;
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

	public void setLinks(Collection<URI> links) {
		this.links = links;
	}

	public Collection<URI> getLinks() {
		return links;
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
		URI uri = getURI();
		for (String href : hrefCollection) {
			href = StringUtils.replace(href, " ", "%20");
			URI resolvedURI = LinkUtils.resolveQuietly(uri, href);
			if (resolvedURI != null)
				uriCollection.add(resolvedURI);
		}
	}


}

/**   
 * License Agreement for QWAZR
 *
 * Copyright (C) 2014-2015 OpenSearchServer Inc.
 * 
 * http://www.qwazr.com
 * 
 * This file is part of QWAZR.
 *
 * QWAZR is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * QWAZR is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with QWAZR. 
 *  If not, see <http://www.gnu.org/licenses/>.
 **/
package com.qwazr.crawler.web.manager;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;

public class CurrentURI {

	final private Integer depth;

	final private URI initialURI;
	private volatile URI finalURI;

	private volatile boolean isIgnored = false;
	private volatile boolean isCrawled = false;
	private volatile boolean isRedirected = false;

	private String error = null;

	private Collection<URI> links = null;

	CurrentURI(URI uri, Integer depth) {
		this.initialURI = uri;
		this.depth = depth;
	}

	public URI getInitialUri() {
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

	void setError(URISyntaxException e) {
		error = e == null ? null : e.getMessage();
	}

	public String getError() {
		return error;
	}

	void setLinks(Collection<URI> links) {
		this.links = links;
	}

	public Collection<URI> getLinks() {
		return links;
	}

}

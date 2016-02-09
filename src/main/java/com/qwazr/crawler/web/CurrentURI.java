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
package com.qwazr.crawler.web;

import java.net.URI;
import java.util.Collection;

public interface CurrentURI {

	/**
	 * @return the initial URI
	 */
	URI getInitialURI();

	/**
	 * The initial URI and the final URI may be different if any redirection was followed
	 *
	 * @return the current URI
	 */
	URI getUri();

	/**
	 * @return the current URI
	 */
	URI getURI();

	/**
	 * @return the depth of the current URL
	 */
	Integer getDepth();

	/**
	 * @return true if the URL matches an inclusion item
	 */
	Boolean isInInclusion();

	/**
	 * @return true if the URL matches an exclusion item
	 */
	Boolean isInExclusion();

	/**
	 * Set the ignored flag
	 *
	 * @param ignored
	 */
	void setIgnored(boolean ignored);

	/**
	 * Check if the URL is ignored. An ignored URL is not crawled
	 *
	 * @return true if the URL is ignored
	 */
	boolean isIgnored();

	/**
	 * Check if the URL has been crawled
	 *
	 * @return true if the URL has been crawled
	 */
	boolean isCrawled();

	/**
	 * Check if the URL has been redirected
	 *
	 * @return true if the URL has been redirected
	 */
	boolean isRedirected();

	String getError();

	void setSameLevelLinks(Collection<URI> links);

	Collection<URI> getSameLevelLinks();

	/**
	 * Replace the next level links
	 *
	 * @param links the new collection
	 */
	void setLinks(Collection<URI> links);

	/**
	 * Get next level links
	 *
	 * @return a collection of links
	 */
	Collection<URI> getLinks();

	void setFilteredLinks(Collection<URI> filteredLinks);

	Collection<URI> getFilteredLinks();

	/**
	 * Check if the domain of the current URL is equals to the domain of the start URL
	 *
	 * @return true if both domains matches
	 */
	boolean isStartDomain();

	/**
	 * Check if the sub-domain of the current URL is equals to the sub-domain of the start URL
	 *
	 * @return true if both sub-domains matches
	 */
	boolean isStartSubDomain();

	/**
	 * Build a collection of URL using a collection of HREF
	 *
	 * @param hrefCollection the HREF collection
	 * @param uriCollection  the URL collection
	 */
	void hrefToURICollection(Collection<String> hrefCollection, Collection<URI> uriCollection);

}

/*
 * Copyright 2015-2017 Emmanuel Keller / QWAZR
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

import com.qwazr.utils.RegExpUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;

public class UBuilder extends URIBuilder {

	public UBuilder(URI uri) {
		super(uri);
	}

	final public void removeMatchingParameters(final Collection<Matcher> matcherList) {
		if (matcherList == null || matcherList.isEmpty())
			return;
		final List<NameValuePair> oldParams = getQueryParams();
		if (oldParams == null || oldParams.isEmpty())
			return;
		clearParameters();
		for (NameValuePair param : oldParams)
			if (!RegExpUtils.anyMatch(param.getName() + "=" + param.getValue(), matcherList))
				addParameter(param.getName(), param.getValue());
	}

	final public void cleanPath(final Collection<Matcher> matcherList) {
		if (matcherList == null || matcherList.isEmpty())
			return;
		String path = getPath();
		if (path == null || path.isEmpty())
			return;
		setPath(RegExpUtils.removeAllMatches(path, matcherList));
	}
}

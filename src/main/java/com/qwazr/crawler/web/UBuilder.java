/*
 * Copyright 2015-2020 Emmanuel Keller / QWAZR
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

import com.qwazr.utils.LinkUtils;
import com.qwazr.utils.RegExpUtils;
import org.glassfish.jersey.uri.internal.JerseyUriBuilder;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;

class UBuilder {

    final private UriBuilder builder;
    final private URI uri;

    UBuilder(URI uri) {
        builder = JerseyUriBuilder.fromUri(uri);
        this.uri = uri;
    }

    final void removeMatchingParameters(final Collection<Matcher> matcherList) throws UnsupportedEncodingException {
        if (matcherList == null || matcherList.isEmpty())
            return;
        final MultivaluedMap<String, String> queryParams = LinkUtils.getQueryParameters(uri.getQuery());
        if (queryParams == null || queryParams.isEmpty())
            return;
        builder.replaceQuery(null);
        queryParams.forEach((key, values) -> {
            final List<String> newValues = new ArrayList<>();
            for (String value : values) {
                if (!RegExpUtils.anyMatch(key + "=" + value, matcherList))
                    newValues.add(value);
            }
            builder.replaceQueryParam(key, newValues.toArray());
        });

    }

    final void cleanPath(final Collection<Matcher> matcherList) {
        if (matcherList == null || matcherList.isEmpty())
            return;
        final String path = uri.getPath();
        if (path == null || path.isEmpty())
            return;
        builder.path(RegExpUtils.removeAllMatches(path, matcherList));
    }

    void removeFragment() {
        builder.fragment(null);
    }

    URI build() {
        return builder.build();
    }
}

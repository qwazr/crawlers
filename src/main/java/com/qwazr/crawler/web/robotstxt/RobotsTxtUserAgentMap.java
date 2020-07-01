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
package com.qwazr.crawler.web.robotstxt;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Contains the list of clauses of a "robots.txt" file per UserAgent
 */
final class RobotsTxtUserAgentMap {

    final Map<String, RobotsTxtClauseSet> clauseMap;

    RobotsTxtUserAgentMap(Builder builder) {
        if (builder.builderMap != null && !builder.builderMap.isEmpty()) {
            clauseMap = new HashMap<>();
            builder.builderMap.forEach((ua, clauses) -> clauseMap.put(ua, clauses.build()));
        } else
            clauseMap = null;
    }

    /**
     * @param userAgent the user-agent to compare
     * @return the right ClauseSet for the passed user-agent
     */
    final protected RobotsTxtClauseSet get(final String userAgent) {
        return clauseMap == null ? null : clauseMap.get(userAgent);
    }

    /**
     * Parse a robots.txt file
     *
     * @param input   the content of the robots.txt file
     * @param charset the charset of the robots.txt file
     * @throws IOException if any I/O error occurs
     */
    static RobotsTxtUserAgentMap of(final InputStream input, final Charset charset) throws IOException {
        final Builder builder = new Builder();
        try (final BufferedReader br = new BufferedReader(new InputStreamReader(input, charset))) {
            String line;
            RobotsTxtClauseSet.Builder currentClauseSet = null;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("#"))
                    continue;
                if (line.length() == 0)
                    continue;
                final StringTokenizer st = new StringTokenizer(line, ":");
                if (!st.hasMoreTokens())
                    continue;
                final String key = st.nextToken().trim();
                String value;
                if (!st.hasMoreTokens())
                    continue;
                value = st.nextToken().trim();
                if ("User-agent".equalsIgnoreCase(key)) {
                    currentClauseSet = builder.userAgent(value.toLowerCase());
                } else if ("Disallow".equalsIgnoreCase(key)) {
                    if (currentClauseSet != null)
                        currentClauseSet.disallow(value);
                } else if ("Allow".equalsIgnoreCase(key)) {
                    if (currentClauseSet != null)
                        currentClauseSet.allow(value);
                }
            }
            return builder.build();
        }
    }

    final static class Builder {

        private Map<String, RobotsTxtClauseSet.Builder> builderMap;

        final RobotsTxtClauseSet.Builder userAgent(String userAgent) {
            if (builderMap == null)
                builderMap = new HashMap<>();
            return builderMap.computeIfAbsent(userAgent, (ua) -> RobotsTxtClauseSet.of());
        }

        final RobotsTxtUserAgentMap build() throws IOException {
            return new RobotsTxtUserAgentMap(this);
        }

    }

}

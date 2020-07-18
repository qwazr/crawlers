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
package com.qwazr.crawler.common;

import com.qwazr.utils.WildcardMatcher;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.validation.constraints.NotNull;

public interface WildcardFilter {

    enum Status {

        accept(null), reject(Rejected.WILDCARD_FILTER);

        private final Rejected rejected;

        Status(Rejected rejected) {
            this.rejected = rejected;
        }
    }

    @NotNull
    static Status definePolicy(final Status explicitPolicy, @NotNull final Map<?, Status> filters) {
        if (explicitPolicy != null)
            return explicitPolicy;
        return filters.isEmpty() ? Status.accept : Status.reject;
    }

    static Map<WildcardMatcher, Status> compileFilters(final Map<String, Status> filters) {
        if (filters == null || filters.isEmpty())
            return Map.of();
        final LinkedHashMap<WildcardMatcher, Status> compiledFilters = new LinkedHashMap<>();
        filters.forEach((pattern, status) -> compiledFilters.put(new WildcardMatcher(pattern), status));
        return Collections.synchronizedMap(compiledFilters);
    }

    static Rejected match(@NotNull final String text,
                          @NotNull final Map<WildcardMatcher, Status> matchers,
                          @NotNull final Status defaultPolicy) {
        for (final Map.Entry<WildcardMatcher, Status> entry : matchers.entrySet())
            if (entry.getKey().match(text))
                return entry.getValue().rejected;
        return defaultPolicy.rejected;
    }

}

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

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.qwazr.utils.Equalizer;
import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(setterVisibility = JsonAutoDetect.Visibility.NONE,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        creatorVisibility = JsonAutoDetect.Visibility.NONE,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE,
        fieldVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY)
public class Rejected extends Equalizer.Immutable<Rejected> {

    public final int code;
    public final String reason;

    @JsonCreator
    public Rejected(@JsonProperty("code") final int code,
                    @JsonProperty("code") final String reason) {
        super(Rejected.class);
        this.code = code;
        this.reason = reason;
    }

    @Override
    protected int computeHashCode() {
        return Objects.hash(code, reason);
    }

    @Override
    protected boolean isEqual(final Rejected rejected) {
        return code == rejected.code && Objects.equals(reason, rejected.reason);
    }

    @Override
    public String toString() {
        return code + " " + reason;
    }

    public final static Rejected WILDCARD_FILTER = new Rejected(1, "Wildcard filter");

}

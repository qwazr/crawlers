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

import java.nio.file.Path;
import org.apache.commons.lang3.StringUtils;

public class CrawlItemBase implements CrawlItem {

    final private int depth;
    final private boolean isIgnored;
    final private boolean isCrawled;
    final private Boolean isInInclusion;
    final private Boolean isInExclusion;
    final private String error;

    protected CrawlItemBase(final BaseBuilder<?> builder) {
        this.depth = builder.depth;
        this.isIgnored = builder.isIgnored;
        this.isCrawled = builder.isCrawled;
        this.isInInclusion = builder.isInInclusion;
        this.isInExclusion = builder.isInExclusion;
        this.error = builder.error;
    }

    @Override
    public int getDepth() {
        return depth;
    }

    @Override
    public Boolean isInInclusion() {
        return isInInclusion;
    }

    @Override
    public Boolean isInExclusion() {
        return isInExclusion;
    }

    @Override
    public boolean isIgnored() {
        return isIgnored;
    }

    @Override
    public boolean isCrawled() {
        return isCrawled;
    }

    @Override
    public String getError() {
        return error;
    }

    protected static abstract class BaseBuilder<T extends BaseBuilder<T>> {

        private final Class<T> builderClass;

        public final int depth;
        private boolean isIgnored;
        private boolean isCrawled;
        private Boolean isInInclusion;
        private Boolean isInExclusion;
        private String error;

        protected BaseBuilder(Class<T> builderClass, int depth) {
            this.builderClass = builderClass;
            this.depth = depth;
        }

        public T ignored(boolean isIgnored) {
            this.isIgnored = isIgnored;
            return builderClass.cast(this);
        }

        public T crawled(boolean isCrawled) {
            this.isCrawled = isCrawled;
            return builderClass.cast(this);
        }

        public T inInclusion(Boolean isInInclusion) {
            this.isInInclusion = isInInclusion;
            return builderClass.cast(this);
        }

        public T inExclusion(Boolean isInExclusion) {
            this.isInExclusion = isInExclusion;
            return builderClass.cast(this);
        }

        public T error(String error) {
            this.error = error;
            return builderClass.cast(this);
        }

        public void error(Exception e) {
            if (e == null) {
                error = null;
                return;
            }
            String err = e.getMessage();
            if (StringUtils.isBlank(err))
                err = e.toString();
            if (StringUtils.isBlank(err))
                err = e.getClass().getName();
            error(err);
        }

    }
}

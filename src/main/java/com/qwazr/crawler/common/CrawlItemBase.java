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

import org.apache.commons.lang3.StringUtils;

public abstract class CrawlItemBase<ITEM> implements CrawlItem<ITEM> {

    final protected ITEM item;
    final protected int depth;
    final protected Rejected rejected;
    final protected String error;

    protected CrawlItemBase(final BaseBuilder<ITEM, ?> builder) {
        this.item = builder.item;
        this.depth = builder.depth;
        this.rejected = builder.rejected;
        this.error = builder.error;
    }

    @Override
    public ITEM getItem() {
        return item;
    }

    @Override
    public int getDepth() {
        return depth;
    }

    @Override
    public Rejected getRejected() {
        return rejected;
    }

    @Override
    public String getError() {
        return error;
    }

    protected static abstract class BaseBuilder<ITEM, BUILDER extends BaseBuilder<ITEM, BUILDER>> {

        public final ITEM item;
        public final int depth;
        private Rejected rejected;
        private String error;

        protected BaseBuilder(ITEM item, int depth) {
            this.item = item;
            this.depth = depth;
        }

        protected abstract BUILDER me();

        public BUILDER rejected(Rejected rejected) {
            this.rejected = rejected;
            return me();
        }

        public BUILDER error(String error) {
            this.error = error;
            return me();
        }

        public BUILDER error(Exception e) {
            if (e == null) {
                error = null;
                return me();
            }
            String err = e.getMessage();
            if (StringUtils.isBlank(err))
                err = e.toString();
            if (StringUtils.isBlank(err))
                err = e.getClass().getName();
            error(err);
            return me();
        }

    }
}

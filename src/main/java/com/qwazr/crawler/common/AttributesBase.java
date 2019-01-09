/*
 * Copyright 2015-2019 Emmanuel Keller / QWAZR
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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AttributesBase implements AttributesInterface {

    protected final ConcurrentHashMap<String, Object> attributes;

    protected AttributesBase() {
        attributes = new ConcurrentHashMap<>();
    }

    protected AttributesBase(Map<String, Object> source) {
        attributes = new ConcurrentHashMap<>(source);
    }

    @Override
    public <A> A getAttribute(final String name, final Class<? extends A> attributeClass) {
        return attributeClass.cast(attributes.get(name));
    }

    @Override
    public <A> A setAttribute(final String name, final A attribute, final Class<? extends A> attributeClass) {
        if (attribute == null)
            return removeAttribute(name, attributeClass);
        return attributeClass.cast(attributes.put(name, attribute));
    }

    @Override
    public <A> A removeAttribute(final String name, final Class<? extends A> attributeClass) {
        return attributeClass.cast(attributes.remove(name));
    }


}

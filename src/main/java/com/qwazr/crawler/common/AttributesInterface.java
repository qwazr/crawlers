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

public interface AttributesInterface {

    /**
     * @param name  the name of the variable to set
     * @param value the attribute to set
     * @param <A>   the type of the attribute
     * @return the previous attribute if any
     */
    <A> A setAttribute(String name, A value, Class<? extends A> attributeClass);

    /**
     * @param name           the name of the attribute
     * @param attributeClass the class of the attribute
     * @param <A>            the type of the attribute
     * @return the attribute if any
     */
    <A> A getAttribute(String name, Class<? extends A> attributeClass);

    /**
     * @param name the name of the attribute to remove
     * @param <A>  the type of the attribute
     * @return the removed attribute if any
     */
    <A> A removeAttribute(String name, Class<? extends A> attributeClass);
    
}

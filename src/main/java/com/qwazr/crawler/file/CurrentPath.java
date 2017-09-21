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
package com.qwazr.crawler.file;

import com.qwazr.crawler.common.CurrentCrawlImpl;
import com.qwazr.utils.StringUtils;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

final public class CurrentPath extends CurrentCrawlImpl {

	final Path path;
	final BasicFileAttributes attributes;
	final String pathString;

	CurrentPath(Builder builder) {
		super(builder);
		this.path = builder.path;
		this.attributes = builder.attributes;
		this.pathString = builder.pathString;
	}

	/**
	 * @return the current path
	 */
	public Path getPath() {
		return path;
	}

	public String getPathString() {
		return pathString;
	}

	/**
	 * @return the attributes of the current path
	 */
	public BasicFileAttributes getAttributes() {
		return attributes;
	}

	final static class Builder extends BaseBuilder<Builder> {

		final Path path;
		final String pathString;
		final BasicFileAttributes attributes;

		Builder(final int depth, final Path path, final BasicFileAttributes attributes) {
			super(Builder.class, depth);
			this.path = path;
			this.attributes = attributes;
			this.pathString = attributes.isDirectory() ?
					StringUtils.ensureSuffix(path.toString(), File.separator) :
					path.toString();
		}

		CurrentPath build() {
			return new CurrentPath(this);
		}
	}
}

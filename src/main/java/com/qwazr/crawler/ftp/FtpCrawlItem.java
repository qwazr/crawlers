/*
 * Copyright 2017-2020 Emmanuel Keller / QWAZR
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
package com.qwazr.crawler.ftp;

import com.qwazr.crawler.common.CrawlItemBase;
import com.qwazr.utils.StringUtils;
import java.nio.file.Path;
import org.apache.commons.net.ftp.FTPFile;

public class FtpCrawlItem extends CrawlItemBase {

    private final FTPFile ftpFile;
    private final String parentPath;
    private final Path localFilePath;

    private FtpCrawlItem(Builder builder) {
        super(builder);
        this.ftpFile = builder.ftpFile;
        this.parentPath = StringUtils.join(builder.parentPath, '/');
        this.localFilePath = builder.localFilePath;
    }

    public String getUser() {
        return ftpFile == null ? null : ftpFile.getUser();
    }

    public String getGroup() {
        return ftpFile == null ? null : ftpFile.getGroup();
    }

    public String getName() {
        return ftpFile == null ? null : ftpFile.getName();
    }

    public Path getLocalFilePath() {
        return localFilePath;
    }

    public String getPath() {
        return ftpFile == null ? null : StringUtils.joinWithSeparator('/', parentPath, ftpFile.getName());
    }

    static class Builder extends BaseBuilder<Builder> {

        private final String parentPath;

        private FTPFile ftpFile;

        private Path localFilePath;

        protected Builder(final String parentPath, final int depth) {
            super(Builder.class, depth);
            this.parentPath = parentPath;
        }

        Builder ftpFile(final FTPFile ftpFile) {
            this.ftpFile = ftpFile;
            return this;
        }

        Builder localFilePath(final Path localFilePath) {
            this.localFilePath = localFilePath;
            return this;
        }

        FtpCrawlItem build() {
            return new FtpCrawlItem(this);
        }
    }
}

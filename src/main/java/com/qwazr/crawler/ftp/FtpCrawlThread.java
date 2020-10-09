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

import com.qwazr.crawler.common.CrawlThread;
import com.qwazr.crawler.common.Rejected;
import com.qwazr.utils.StringUtils;
import com.qwazr.utils.concurrent.RunnableEx;
import com.qwazr.utils.concurrent.SupplierEx;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.ftp.FTPSClient;

public class FtpCrawlThread extends CrawlThread
        <FtpCrawlThread, FtpCrawlDefinition, FtpCrawlSessionStatus, FtpCrawlerManager, FtpCrawlSession, FtpCrawlItem> {

    private final FtpCrawlDefinition crawlDefinition;
    private final FTPClient ftp;

    FtpCrawlThread(final FtpCrawlerManager manager, final FtpCrawlSession session, final Logger logger) {
        super(manager, session, logger);
        this.crawlDefinition = session.getCrawlDefinition();
        if (crawlDefinition.isSsl != null && crawlDefinition.isSsl)
            ftp = new FTPSClient();
        else
            ftp = new FTPClient();
        ftp.setConnectTimeout(60000);
        ftp.setDataTimeout(60000);
    }

    @Override
    protected void runner() throws IOException {
        Objects.requireNonNull(crawlDefinition.hostname, "The host name of the server is missing");
        try {

            // Connection
            checkPositiveReply(() -> ftp.connect(crawlDefinition.hostname, crawlDefinition.port == null ? 21 : crawlDefinition.port),
                    (code, msg) -> "FTP server refused connection (" + code + "): " + msg);

            // Login
            checkPositiveReply(() -> ftp.login(
                    StringUtils.isBlank(crawlDefinition.username) ? "anonymous" : crawlDefinition.username,
                    StringUtils.isBlank(crawlDefinition.password) ? "guest" : crawlDefinition.password),
                    (code, msg) -> "Cannot login as " + crawlDefinition.username + " - " + msg + " (" + code + ')');

            // Let crawl the current directory
            // Change directory
            checkTransferMode();
            if (!StringUtils.isBlank(crawlDefinition.entryPath))
                checkPositiveReply(() -> ftp.changeWorkingDirectory(crawlDefinition.entryPath),
                        (code, msg) -> "Cannot change the directory to " + crawlDefinition.entryPath + " - : " + msg + " (" + code + ')');

            listCurrentDirectory(crawlDefinition.entryPath, 0);

            // Finished, we logout
            ftp.logout();
        } finally {
            if (ftp.isConnected()) {
                try {
                    ftp.disconnect();
                } catch (IOException ioe) {
                    logger.log(Level.WARNING, ioe, ioe::getMessage);
                }
            }
        }
    }

    private void checkTransferMode() throws IOException {
        // Do we switch to passive mode ?
        if (crawlDefinition.isPassive != null && crawlDefinition.isPassive)
            checkPositiveReply(ftp::enterLocalPassiveMode,
                    (code, msg) -> "Passive mode failed: " + msg + " (" + code + ')');
        else
            checkPositiveReply(ftp::enterLocalActiveMode,
                    (code, msg) -> "Passive mode failed: " + msg + " (" + code + ')');
    }

    private void listCurrentDirectory(final String currentPath, int depth) throws IOException {
        if (session.isAborting())
            return;

        final FTPFile[] ftpFiles = ftp.listFiles();
        if (ftpFiles == null || ftpFiles.length == 0)
            return;

        // First pass we only want files
        for (final FTPFile ftpFile : ftpFiles) {
            if (ftpFile.isFile()) {
                final String filePath = '/' + StringUtils.joinWithSeparator('/', currentPath, ftpFile.getName());
                crawlFile(ftpFile, new FtpCrawlItem.Builder(filePath, currentPath, depth));
            }
        }

        // Second pass, we manage the directories
        final int nextDepth = depth + 1;
        for (final FTPFile ftpFile : ftpFiles) {
            if (".".equals(ftpFile.getName()) || "..".equals(ftpFile.getName()))
                continue;
            if (ftpFile.isDirectory()) {
                final String directoryName = ftpFile.getName();
                final String nextPath = '/' + StringUtils.joinWithSeparator('/', currentPath, directoryName) + '/';
                final FtpCrawlItem.Builder nextBuilder = new FtpCrawlItem.Builder(nextPath, currentPath, depth);
                if (checkWildcardFilters(nextPath) != null) {
                    logger.info("Ignore FTP directory: " + nextBuilder.item);
                    continue;
                }

                checkPositiveReply(() -> ftp.changeWorkingDirectory(directoryName),
                        (code, msg) -> "Cannot change the directory to " + directoryName + " - : " + msg + " (" + code +
                                ')');
                listCurrentDirectory(nextPath, nextDepth);
                ftp.changeToParentDirectory();
            }
        }
    }

    private void crawlFile(final FTPFile ftpFile, final FtpCrawlItem.Builder builder) throws IOException {
        if (session.isAborting())
            return;

        builder.ftpFile(ftpFile);

        final FtpCrawlItem currentCrawl = builder.build();

        final Rejected rejected = checkWildcardFilters(currentCrawl.getItem());
        if (rejected != null) {
            builder.rejected(rejected);
            logger.info("Ignore FTP file: " + builder.item);
            session.incRejectedCount();
            session.collect(builder.build());
            return;
        }

        logger.info("Download FTP file: " + builder.item);

        checkTransferMode();
        final Path tmpFile = Files.createTempFile("ftpCrawler-", ftpFile.getName());
        try {
            try {
                try (final OutputStream output = new BufferedOutputStream(Files.newOutputStream(tmpFile))) {
                    ftp.retrieveFile(ftpFile.getName(), output);
                }
                builder.localFilePath(tmpFile);
            } catch (RuntimeException | IOException e) {
                builder.error(e);
                throw e;
            }
            session.incCrawledCount();
            session.collect(builder.build());
        } finally {
            Files.deleteIfExists(tmpFile);
        }
    }

    private void checkPositiveReply(final RunnableEx<IOException> action,
                                    final BiFunction<Integer, String, String> errorMessage) throws IOException {
        action.run();
        final int code = ftp.getReplyCode();
        final String msg = ftp.getReplyString();
        if (!FTPReply.isPositiveCompletion(code))
            throw new IOException(errorMessage.apply(code, msg));
    }

    private void checkPositiveReply(final SupplierEx<Boolean, IOException> action,
                                    final BiFunction<Integer, String, String> errorMessage) throws IOException {
        if (!action.get())
            throw new IOException(errorMessage.apply(ftp.getReplyCode(), ftp.getReplyString()));
    }

}

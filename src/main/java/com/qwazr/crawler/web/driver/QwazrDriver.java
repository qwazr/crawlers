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
package com.qwazr.crawler.web.driver;

import com.google.common.net.HttpHeaders;
import com.qwazr.crawler.web.ProxyDefinition;
import com.qwazr.crawler.web.WebCrawlDefinition;
import com.qwazr.crawler.web.WebRequestDefinition;
import com.qwazr.utils.IOUtils;
import com.qwazr.utils.LoggerUtils;
import com.qwazr.utils.RandomUtils;
import com.qwazr.utils.StringUtils;
import okhttp3.Call;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.apache.commons.lang3.NotImplementedException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class QwazrDriver implements DriverInterface {

    private final static Logger LOGGER = LoggerUtils.getLogger(QwazrDriver.class);

    private final OkHttpClient client;

    private final String userAgent;

    private final ConcurrentHashMap.KeySetView<Body, Boolean> bodies;

    QwazrDriver(final WebCrawlDefinition definition) {
        final OkHttpClient.Builder builder = new OkHttpClient.Builder().followRedirects(false)
                .followSslRedirects(false)
                .retryOnConnectionFailure(true);

        final int timeOutSecs = definition.timeOutSecs == null ? 120 : definition.timeOutSecs;
        builder.connectTimeout(timeOutSecs, TimeUnit.SECONDS)
                .readTimeout(timeOutSecs, TimeUnit.SECONDS)
                .writeTimeout(timeOutSecs, TimeUnit.SECONDS);

        if (definition.proxies != null && !definition.proxies.isEmpty()) {
            final List<ProxyDefinition> proxyList = definition.proxies.stream()
                    .filter(p -> p.enabled == null || p.enabled)
                    .collect(Collectors.toList());
            if (!proxyList.isEmpty()) {
                final ProxyDefinition proxy = proxyList.get(RandomUtils.nextInt(0, proxyList.size()));
                if (!StringUtils.isBlank(proxy.httpProxy)) {
                    builder.proxy(getProxyHostPort(Proxy.Type.HTTP, proxy.httpProxy));
                } else if (!StringUtils.isBlank(proxy.socksProxy)) {
                    builder.proxy(getProxyHostPort(Proxy.Type.SOCKS, proxy.socksProxy));
                }
            }
        }

        if (definition.cookies != null && !definition.cookies.isEmpty())
            builder.cookieJar(new Cookies(definition.cookies));

        userAgent = StringUtils.isBlank(definition.userAgent) ? null : definition.userAgent;
        bodies = ConcurrentHashMap.newKeySet();
        client = builder.build();
    }

    Proxy getProxyHostPort(Proxy.Type type, String hostPort) {
        final String[] parts = StringUtils.split(hostPort, ":");
        if (parts.length != 2)
            throw new IllegalArgumentException(
                    "Bad syntax in proxy definition Expected \"host:port\" but got: " + hostPort);
        final String host = parts[0];
        final int port;
        try {
            port = Integer.parseInt(parts[1]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Bad syntax in proxy definition Expected \"host:port\" but got: " + hostPort, e);
        }
        return new Proxy(type, InetSocketAddress.createUnresolved(host, port));
    }

    private void cancel(Call call, String reason) {
        LOGGER.warning(() -> "Call cancelled: " + reason);
        call.cancel();
    }

    @Override
    public Head head(WebRequestDefinition request) throws IOException {
        return new HeadImpl(request);
    }

    @Override
    public Body body(WebRequestDefinition request) throws IOException {
        final WebRequestDefinition.HttpMethod method =
                request.method == null ? WebRequestDefinition.HttpMethod.GET : request.method;
        final Body body;
        switch (method) {
            case GET:
                body = new GetImpl(request);
                break;
            case POST:
                body = new PostImpl(request);
                break;
            default:
                throw new NotImplementedException("Method not supported: " + method);
        }
        bodies.add(body);
        return body;
    }

    @Override
    public void close() throws IOException {
        final List<Body> toClose = new ArrayList<>(bodies);
        for (Body body : toClose)
            body.close();
        assert bodies.isEmpty();
    }

    static Long buildContentLength(String header) {
        return header == null ? null : Long.parseLong(header);
    }

    static String buildContentType(String header) {
        if (StringUtils.isBlank(header))
            return null;
        final MediaType mediaType = MediaType.parse(header);
        return mediaType == null ? null : mediaType.type() + '/' + mediaType.subtype();
    }

    class HeadImpl implements Head {

        final WebRequestDefinition request;
        final int responseCode;
        final Headers headers;
        final ContentImpl content;
        final String redirectLocation;
        final String contentType;
        final Long contentLength;
        final String contentEncoding;
        final boolean isSuccessful;

        HeadImpl(final WebRequestDefinition request) throws IOException {
            this.request = request;
            final Request.Builder builder = new Request.Builder().url(request.url);
            if (userAgent != null)
                builder.header(HttpHeaders.USER_AGENT, userAgent);
            if (request.headers != null)
                request.headers.forEach(builder::header);
            request(builder);
            try (final Response response = client.newCall(builder.build()).execute()) {
                responseCode = response.code();
                headers = response.headers();
                content = response(response);
                isSuccessful = response.isSuccessful();
                if (headers != null) {
                    redirectLocation = response.isRedirect() ? headers.get(HttpHeaders.LOCATION) : null;
                    contentType = buildContentType(headers.get(HttpHeaders.CONTENT_TYPE));
                    contentLength = buildContentLength(headers.get(HttpHeaders.CONTENT_LENGTH));
                    contentEncoding = headers.get(HttpHeaders.CONTENT_ENCODING);
                } else {
                    redirectLocation = null;
                    contentType = null;
                    contentLength = null;
                    contentEncoding = null;
                }
            }
        }

        void request(Request.Builder builder) {
            builder.head();
        }

        ContentImpl response(Response response) throws IOException {
            return null;
        }

        @Override
        final public String getUrl() {
            return request.url;
        }

        @Override
        final public int getResponseCode() {
            return responseCode;
        }

        @Override
        final public boolean isSuccessful() {
            return isSuccessful;
        }

        @Override
        final public Long getContentLength() {
            return contentLength;
        }

        @Override
        final public String getContentType() {
            return contentType;
        }

        @Override
        final public String getContentEncoding() {
            return contentEncoding;
        }

        @Override
        final public List<String> getHeaders(String name) {
            return headers == null ? null : headers.values(name);
        }

        @Override
        final public String getFirstHeader(String name) {
            return headers == null ? null : headers.get(name);
        }

        @Override
        final public String getRedirectLocation() {
            return redirectLocation;
        }
    }

    abstract class BodyImpl extends HeadImpl implements Body {

        private volatile Document document;

        BodyImpl(WebRequestDefinition request) throws IOException {
            super(request);
        }

        @Override
        ContentImpl response(Response response) throws IOException {
            return new ContentImpl(response);
        }

        @Override
        public Content getContent() {
            return content;
        }

        @Override
        public void close() throws IOException {
            if (content != null && !content.isClosed())
                content.close();
            bodies.remove(this);
        }

        public synchronized Document getHtmlDocument() throws IOException {
            if (document != null)
                return document;
            if (content == null)
                return null;
            if (!"text/html".equals(content.getContentType()))
                return null;
            try (final InputStream input = content.getInput()) {
                document = Jsoup.parse(input, content.getCharsetName(), getUrl());
            }
            return document;
        }
    }

    final class GetImpl extends BodyImpl {

        GetImpl(final WebRequestDefinition request) throws IOException {
            super(request);
        }

        @Override
        void request(Request.Builder builder) {
            builder.get();
        }
    }

    final class PostImpl extends BodyImpl {

        PostImpl(final WebRequestDefinition request) throws IOException {
            super(request);
        }

        @Override
        void request(Request.Builder builder) {
            final FormBody.Builder formBodyBuilder = new FormBody.Builder();
            if (request.parameters != null)
                request.parameters.forEach((name, values) -> {
                    if (values != null)
                        values.forEach(value -> formBodyBuilder.add(name, value));
                });
            builder.post(formBodyBuilder.build());
        }

    }

    static class ContentImpl implements Content, Closeable {

        final Path contentCache;
        final String contentType;
        final Charset charset;
        final Long contentLength;

        ContentImpl(Response response) throws IOException {
            contentCache = Files.createTempFile("QwazrDriver", ".cache");
            try (ResponseBody responseBody = response.body()) {
                if (responseBody != null) {
                    contentLength = responseBody.contentLength();
                    final MediaType mediaType = responseBody.contentType();
                    if (mediaType != null) {
                        contentType = mediaType.type() + '/' + mediaType.subtype();
                        charset = mediaType.charset();
                    } else {
                        contentType = null;
                        charset = null;
                    }
                    try (final InputStream input = responseBody.byteStream()) {
                        IOUtils.copy(input, contentCache);
                    }
                } else {
                    contentLength = null;
                    contentType = null;
                    charset = null;
                }
            } catch (IOException e) {
                Files.deleteIfExists(contentCache);
                throw e;
            }
        }

        @Override
        public void close() throws IOException {
            Files.deleteIfExists(contentCache);
        }

        @Override
        public boolean isClosed() {
            return contentCache == null || !Files.exists(contentCache);
        }

        @Override
        public InputStream getInput() throws IOException {
            return new BufferedInputStream(Files.newInputStream(contentCache));
        }

        @Override
        public String getContentType() {
            return contentType;
        }

        @Override
        public String getCharsetName() {
            return charset == null ? null : charset.name();
        }

        @Override
        public Charset getCharset() {
            return charset;
        }

        @Override
        public Long getContentLength() {
            return contentLength;
        }
    }

    static class Cookies implements CookieJar {

        private final List<Cookie> cookieList;

        Cookies(final Map<String, String> cookies) {
            cookieList = new ArrayList<>(cookies.size());
            cookies.forEach((name, value) -> cookieList.add(new Cookie.Builder().name(name).value(value).build()));
        }

        @Override
        public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
            //The purpose of this class is to set the cookie defined by the WebCrawlerDefinition
        }

        @Override
        public List<Cookie> loadForRequest(HttpUrl url) {
            return cookieList;
        }
    }

}

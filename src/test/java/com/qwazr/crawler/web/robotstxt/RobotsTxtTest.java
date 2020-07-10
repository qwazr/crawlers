/*
 * Copyright 2017 Emmanuel Keller / QWAZR
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
package com.qwazr.crawler.web.robotstxt;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

public class RobotsTxtTest {

    void shouldMatch(String pattern, String... paths) {
        final RobotsTxtPathMatcher matcher = RobotsTxtPathMatcher.of(pattern);
        Assert.assertEquals(pattern, matcher.getPattern());
        for (String path : paths)
            Assert.assertTrue("Path: " + path + " - Pattern: " + pattern, matcher.match(path));
    }

    void shouldNotMatch(String pattern, String... paths) {
        final RobotsTxtPathMatcher matcher = RobotsTxtPathMatcher.of(pattern);
        Assert.assertEquals(pattern, matcher.getPattern());
        for (String path : paths)
            Assert.assertFalse("Path: " + path + " - Pattern: " + pattern, matcher.match(path));
    }

    @Test
    public void pathMatcherTests() {
        shouldMatch("/", "/", "/any");
        shouldMatch("/*", "/", "/any");

        shouldMatch("/fish", "/fish", "/fish.html", "/fish/salmon.html", "/fishheads", "/fishheads/yummy.html",
                "/fish.php?id=anything");
        shouldNotMatch("/fish", "/Fish.asp", "/catfish", "/?id=fish");

        shouldMatch("/fish*", "/fish", "/fish.html", "/fish/salmon.html", "/fishheads", "/fishheads/yummy.html",
                "/fish.php?id=anything");
        shouldNotMatch("/fish*", "/Fish.asp", "/catfish", "/?id=fish");

        shouldMatch("/fish/", "/fish/", "/fish/?id=anything", "/fish/salmon.htm");
        shouldNotMatch("/fish/", "/fish", "/fish.html", "/Fish/Salmon.asp");

        shouldMatch("/*.php", "/filename.php", "/folder/filename.php", "/folder/filename.php?parameters",
                "/folder/any.php.file.html", "/filename.php/");
        shouldNotMatch("/*.php", "/", "/windows.PHP");

        shouldMatch("/*.php$", "/filename.php", "/folder/filename.php");
        shouldNotMatch("/*.php$", "/filename.php?parameters", "/filename.php/", "/filename.php5", "/windows.PHP");

        shouldMatch("/fish*.php", "/fish.php", "/fishheads/catfish.php?parameters");
        shouldNotMatch("/fish*.php", "/Fish.PHP");
    }

    public void checkAllowDisallow(String url, String allow, String disallow, RobotsTxt.Status status)
            throws IOException, URISyntaxException {
        Assert.assertEquals(status, new RobotsTxt(
                IOUtils.toInputStream("user-agent: *\nAllow: " + allow + "\nDisallow: " + disallow,
                        StandardCharsets.UTF_8), StandardCharsets.UTF_8).getStatus(URI.create(url), "ua"));
    }

    @Test
    public void allowDisallowTests() throws IOException, URISyntaxException {
        checkAllowDisallow("http://example.com/page", "/p", "/", RobotsTxt.Status.ALLOW);
        checkAllowDisallow("http://example.com/folder/page", "/folder/", "/folder", RobotsTxt.Status.ALLOW);
        //checkAllowDisallow("http://example.com/page.htm", "/page", "/*.htm", RobotsTxt.Status.ALLOW);
        checkAllowDisallow("http://example.com/", "/$", "/", RobotsTxt.Status.ALLOW);
        checkAllowDisallow("http://example.com/page.htm", "/$", "/", RobotsTxt.Status.DISALLOW);
    }
}

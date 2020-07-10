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
package com.qwazr.crawler.file;

import com.qwazr.utils.ObjectMappers;
import com.qwazr.utils.RandomUtils;
import com.qwazr.utils.TimeTracker;
import java.io.IOException;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import org.junit.jupiter.api.Test;

public class FileCrawlerStatusTest {

    @Test
    public void serializationTest() throws IOException {
        final FileCrawlStatus status = FileCrawlStatus.of("test", TimeTracker.withDurations())
                .incRedirect()
                .incCrawled()
                .incIgnored()
                .lastError(RandomUtils.alphanumeric(8))
                .crawl("test", 5)
                .build();
        final byte[] bytes = ObjectMappers.SMILE.writeValueAsBytes(status);
        final FileCrawlStatus status2 = ObjectMappers.SMILE.readValue(bytes, FileCrawlStatus.class);
        assertThat(status, equalTo(status2));
    }
}

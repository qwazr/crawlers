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
package com.qwazr.crawler.common;

import com.qwazr.server.client.ErrorWrapper;
import com.qwazr.utils.WaitFor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class CrawlHelpers {

    public static CrawlSessionStatus<?> crawlWait(final String sessionName, final CrawlerServiceInterface<?, ?> service)
            throws InterruptedException {
        final AtomicReference<CrawlSessionStatus<?>> statusRef = new AtomicReference<>();
        WaitFor.of().timeOut(TimeUnit.MINUTES, 2).until(() -> {
            final CrawlSessionStatus<?> status = ErrorWrapper.bypass(
                    () -> service.getSessionStatus(sessionName), 404);
            statusRef.set(status);
            return status != null && status.endTime != null;
        });
        return statusRef.get();
    }


}

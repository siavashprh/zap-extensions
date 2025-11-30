/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2024 The ZAP Development Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zaproxy.addon.reportingproxy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.parosproxy.paros.network.HttpMessage;

/**
 * A rule that detects if a specific domain receives too many requests within a short time window.
 */
public class RateLimitRule implements ReportingRule {

    private static final int MAX_REQUESTS = 3;
    private static final long TIME_WINDOW_MS = 10000; // 10 seconds

    // Map to store request timestamps for each host
    private final Map<String, List<Long>> requestTimestamps = new HashMap<>();

    @Override
    public String getName() {
        return "Rate Limiting Rule";
    }

    @Override
    public synchronized String check(HttpMessage msg) {
        String host = msg.getRequestHeader().getHostName();
        if (host == null) {
            return null;
        }

        long currentTime = System.currentTimeMillis();
        List<Long> timestamps = requestTimestamps.computeIfAbsent(host, k -> new ArrayList<>());

        // Add current timestamp
        timestamps.add(currentTime);

        // Remove old timestamps
        Iterator<Long> iterator = timestamps.iterator();
        while (iterator.hasNext()) {
            Long timestamp = iterator.next();
            if (currentTime - timestamp > TIME_WINDOW_MS) {
                iterator.remove();
            }
        }

        // Check for violation
        if (timestamps.size() > MAX_REQUESTS) {
            return "Rate limit exceeded for host: "
                    + host
                    + ". More than "
                    + MAX_REQUESTS
                    + " requests in "
                    + (TIME_WINDOW_MS / 1000)
                    + " seconds.";
        }

        return null;
    }
}

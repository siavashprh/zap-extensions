/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2025 The ZAP Development Team
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
package org.zaproxy.addon.reportingproxy.rules;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.addon.reportingproxy.NotificationService;
import org.zaproxy.addon.reportingproxy.ReportingRule;

/**
 * Detects if the number of requests to a certain domain exceeds a threshold in a time window.
 * 
 * @param msg The HTTP message to scan.
 */
public class RateLimitRule extends ReportingRule {

    private static final int THRESHOLD = 10;
    private static final long TIME_WINDOW = 10000; // 10 seconds

    private Map<String, Queue<Long>> requestHistory = new HashMap<>();

    @Override
    public void scan(HttpMessage msg) {
        // Only care about requests
        if (msg.getResponseHeader().isEmpty()) {
            String domain = msg.getRequestHeader().getHostName();
            if (domain == null) {
                return;
            }
            long now = getCurrentTime();

            requestHistory.putIfAbsent(domain, new LinkedList<>());
            Queue<Long> timestamps = requestHistory.get(domain);

            // Add current timestamp
            timestamps.add(now);

            // Remove old timestamps
            while (!timestamps.isEmpty() && now - timestamps.peek() > TIME_WINDOW) {
                timestamps.poll();
            }

            // Check threshold
            if (timestamps.size() > THRESHOLD) {
                notifyViolation(
                        msg,
                        "Rate limit exceeded for "
                                + domain
                                + ". "
                                + timestamps.size()
                                + " requests in "
                                + (TIME_WINDOW / 1000)
                                + "s.");
            }
        }
    }

    protected void notifyViolation(HttpMessage msg, String details) {
        if (getNotificationService() != null) {
            getNotificationService().notify(this, msg, details);
        }
    }

    protected long getCurrentTime() {
        return System.currentTimeMillis();
    }

    @Override
    public String getName() {
        return "Rate Limit Rule";
    }

    @Override
    public String getDescription() {
        return "Detects if request count to a domain exceeds threshold in a time window.";
    }
}

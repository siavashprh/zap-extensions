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

import java.net.HttpCookie;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.parosproxy.paros.network.HttpHeader;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.addon.reportingproxy.NotificationService;
import org.zaproxy.addon.reportingproxy.ReportingRule;

/**
 * Detects if cookie values set in responses are subsequently found in outgoing request
 * URLs, which may indicate cookie syncing or tracking.
 * 
 * @param msg The HTTP message to scan.
 */
public class CookieSyncRule extends ReportingRule {

    // Thread-safe set to store unique cookie values
    private final Set<String> trackedCookies = Collections.synchronizedSet(new HashSet<>());

    @Override
    public void scan(HttpMessage msg) {
        if (msg.getResponseHeader().isEmpty()) {
            // Check Mode (Request)
            checkRequest(msg);
        } else {
            // Capture Mode (Response)
            captureCookies(msg);
        }
    }

    private void captureCookies(HttpMessage msg) {
        List<String> cookies = msg.getResponseHeader().getHeaderValues(HttpHeader.SET_COOKIE);
        if (cookies == null) {
            return;
        }

        for (String cookieHeader : cookies) {
            // Parse cookie to extract value and ignore attributes
            try {
                // HttpCookie.parse handles the complex parsing of attributes
                List<HttpCookie> parsedCookies = HttpCookie.parse(cookieHeader);
                for (HttpCookie cookie : parsedCookies) {
                    String value = cookie.getValue();
                    // Ignore empty or very short values to avoid false positives
                    if (value != null && value.length() > 5) {
                        trackedCookies.add(value);
                    }
                }
            } catch (IllegalArgumentException e) {
                // Ignore malformed cookies
            }
        }
    }

    private void checkRequest(HttpMessage msg) {
        String url = msg.getRequestHeader().getURI().toString();
        
        // Iterate over a snapshot or handle concurrency safely
        // synchronized block is safest for iteration over synchronizedSet
        synchronized (trackedCookies) {
            for (String cookieValue : trackedCookies) {
                if (url.contains(cookieValue)) {
                    notifyViolation(msg, "Potential Cookie Syncing detected. URL contains tracked cookie value: " + cookieValue);
                    // Report once per request is sufficient
                    break;
                }
            }
        }
    }

    protected void notifyViolation(HttpMessage msg, String details) {
        if (getNotificationService() != null) {
            getNotificationService().notify(this, msg, details);
        }
    }

    @Override
    public String getName() {
        return "Cookie Syncing Rule";
    }

    @Override
    public String getDescription() {
        return "Detects if cookie values set in responses are subsequently found in outgoing request URLs.";
    }
}

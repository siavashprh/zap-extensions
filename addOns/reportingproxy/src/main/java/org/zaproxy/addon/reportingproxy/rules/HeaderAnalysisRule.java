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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.parosproxy.paros.network.HttpHeader;
import org.parosproxy.paros.network.HttpHeaderField;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.addon.reportingproxy.NotificationService;
import org.zaproxy.addon.reportingproxy.ReportingRule;

/**
 * Detects when common security headers (like HSTS) are missing
 * 
 * @param msg The HTTP message to scan.
 */
public class HeaderAnalysisRule extends ReportingRule {

    private static final int HISTORY_SIZE = 5;
    // Domain -> List of Header Sets (newest last)
    private final Map<String, LinkedList<Set<String>>> history = new HashMap<>();

    @Override
    public void scan(HttpMessage msg) {
        if (msg.getResponseHeader().isEmpty()) {
            return;
        }

        String host = msg.getRequestHeader().getHostName();
        if (host == null) {
            return;
        }

        Set<String> currentHeaders = getHeaderNames(msg.getResponseHeader());
        
        LinkedList<Set<String>> domainHistory = history.computeIfAbsent(host, k -> new LinkedList<>());
        
        if (domainHistory.size() >= HISTORY_SIZE) {
            Set<String> commonHeaders = new HashSet<>(domainHistory.getFirst());
            for (Set<String> headers : domainHistory) {
                commonHeaders.retainAll(headers);
            }

            for (String header : commonHeaders) {
                if (!currentHeaders.contains(header)) {
                    notifyViolation(msg, "Missing common security header: " + header);
                    break;
                }
            }
        }

        domainHistory.add(currentHeaders);
        if (domainHistory.size() > HISTORY_SIZE) {
            domainHistory.removeFirst();
        }
    }

    protected void notifyViolation(HttpMessage msg, String details) {
        if (getNotificationService() != null) {
            getNotificationService().notify(this, msg, details);
        }
    }

    private Set<String> getHeaderNames(HttpHeader header) {
        Set<String> names = new HashSet<>();
        List<HttpHeaderField> fields = header.getHeaders();
        if (fields != null) {
            for (HttpHeaderField field : fields) {
                names.add(field.getName());
            }
        }
        return names;
    }

    @Override
    public String getName() {
        return "Missing Security Header Rule";
    }

    @Override
    public String getDescription() {
        return "Detects when common security headers (e.g. HSTS) are missing from a response after being present in recent history.";
    }
}

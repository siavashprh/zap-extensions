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

import java.util.List;
import org.parosproxy.paros.network.HttpHeader;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.addon.reportingproxy.NotificationService;
import org.zaproxy.addon.reportingproxy.ReportingRule;

/**
 * A rule that detects if a Content Security Policy (CSP) is missing from HTML responses.
 * It checks both the HTTP 'Content-Security-Policy' header and the <meta> tag in the body.
 */
public class CspDetectionRule implements ReportingRule {

    @Override
    public void scan(HttpMessage msg) {
        // Only analyze responses
        if (msg.getResponseHeader().isEmpty()) {
            return;
        }

        // Only analyze HTML content
        String contentType = msg.getResponseHeader().getHeader(HttpHeader.CONTENT_TYPE);
        if (contentType == null || !contentType.toLowerCase().contains("text/html")) {
            return;
        }

        // Check for CSP Header
        List<String> cspHeaders = msg.getResponseHeader().getHeaderValues("Content-Security-Policy");
        if (cspHeaders != null && !cspHeaders.isEmpty()) {
            return; // CSP found in header
        }

        // Check for CSP Meta Tag in Body
        String responseBody = msg.getResponseBody().toString();
        if (responseBody.toLowerCase().contains("<meta http-equiv=\"content-security-policy\"")) {
            return; // CSP found in meta tag
        }

        // If we get here, CSP is missing
        notifyViolation(msg, "Missing Content Security Policy (CSP) in HTML response.");
    }

    protected void notifyViolation(HttpMessage msg, String details) {
        NotificationService.getSingleton().notify(this, msg, details);
    }

    @Override
    public String getName() {
        return "CSP Detection Rule";
    }

    @Override
    public String getDescription() {
        return "Detects missing Content Security Policy (CSP) in HTML responses.";
    }
}

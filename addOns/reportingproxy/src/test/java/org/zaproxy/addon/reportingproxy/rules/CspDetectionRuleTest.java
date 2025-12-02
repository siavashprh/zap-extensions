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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.parosproxy.paros.network.HttpHeader;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpRequestHeader;
import org.parosproxy.paros.network.HttpResponseHeader;


class CspDetectionRuleTest {

    private TestableCspDetectionRule rule;
    private HttpMessage msg;
    private HttpRequestHeader reqHeader;
    private HttpResponseHeader respHeader;

    @BeforeEach
    void setUp() {
        rule = new TestableCspDetectionRule();
        msg = new HttpMessage(); // Use real object to avoid mocking hell with body
        reqHeader = msg.getRequestHeader();
        respHeader = msg.getResponseHeader();
    }

    @Test
    void shouldIgnoreNonHtml() {
        respHeader.setHeader(HttpHeader.CONTENT_TYPE, "application/json");
        rule.scan(msg);
        assertFalse(rule.violationReported);
    }

    @Test
    void shouldReportMissingCsp() throws Exception {
        respHeader.setMessage("HTTP/1.1 200 OK\r\n");
        respHeader.setHeader(HttpHeader.CONTENT_TYPE, "text/html");
        msg.setResponseBody("<html><body>No CSP here</body></html>");
        
        rule.scan(msg);
        
        assertTrue(rule.violationReported);
        assertTrue(rule.violationDetails.contains("Missing Content Security Policy"));
    }

    @Test
    void shouldNotReportIfCspHeaderPresent() {
        respHeader.setHeader(HttpHeader.CONTENT_TYPE, "text/html");
        respHeader.setHeader("Content-Security-Policy", "default-src 'self'");
        msg.setResponseBody("<html><body>CSP in header</body></html>");
        
        rule.scan(msg);
        
        assertFalse(rule.violationReported);
    }

    @Test
    void shouldNotReportIfCspMetaTagPresent() {
        respHeader.setHeader(HttpHeader.CONTENT_TYPE, "text/html");
        msg.setResponseBody("<html><head><meta http-equiv=\"Content-Security-Policy\" content=\"default-src 'self'\"></head><body>CSP in meta</body></html>");
        
        rule.scan(msg);
        
        assertFalse(rule.violationReported);
    }

    // Subclass to capture notifications
    private static class TestableCspDetectionRule extends CspDetectionRule {
        boolean violationReported = false;
        String violationDetails = "";

        @Override
        protected void notifyViolation(HttpMessage msg, String details) {
            violationReported = true;
            violationDetails = details;
        }
    }
}

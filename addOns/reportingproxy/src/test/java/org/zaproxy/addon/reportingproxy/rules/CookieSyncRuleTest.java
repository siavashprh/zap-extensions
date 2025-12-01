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
import org.apache.commons.httpclient.URI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.parosproxy.paros.network.HttpHeader;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpRequestHeader;
import org.parosproxy.paros.network.HttpResponseHeader;

class CookieSyncRuleTest {

    private TestableCookieSyncRule rule;
    private HttpMessage msg;
    private HttpRequestHeader reqHeader;
    private HttpResponseHeader respHeader;

    @BeforeEach
    void setUp() {
        rule = new TestableCookieSyncRule();
        msg = mock(HttpMessage.class);
        reqHeader = mock(HttpRequestHeader.class);
        respHeader = mock(HttpResponseHeader.class);
        when(msg.getRequestHeader()).thenReturn(reqHeader);
        when(msg.getResponseHeader()).thenReturn(respHeader);
    }

    @Test
    void shouldCaptureCookieValue() throws Exception {
        // Capture Mode
        when(respHeader.isEmpty()).thenReturn(false);
        when(respHeader.getHeaderValues(HttpHeader.SET_COOKIE)).thenReturn(List.of("session_id=abcdef123456"));
        
        rule.scan(msg);
        
        // Check Mode (verify capture by triggering it)
        when(respHeader.isEmpty()).thenReturn(true);
        when(reqHeader.getURI()).thenReturn(new URI("http://tracker.com?data=abcdef123456", true));
        
        rule.scan(msg);
        
        assertTrue(rule.violationReported);
        assertTrue(rule.violationDetails.contains("abcdef123456"));
    }

    @Test
    void shouldHandleCookieAttributes() throws Exception {
        // Capture Mode with attributes
        when(respHeader.isEmpty()).thenReturn(false);
        when(respHeader.getHeaderValues(HttpHeader.SET_COOKIE)).thenReturn(List.of("tracking_user=user_xyz_789; Domain=example.com; Path=/; Secure; HttpOnly"));
        
        rule.scan(msg);
        
        // Check Mode
        when(respHeader.isEmpty()).thenReturn(true);
        when(reqHeader.getURI()).thenReturn(new URI("http://adnetwork.com/sync?uid=user_xyz_789", true));
        
        rule.scan(msg);
        
        assertTrue(rule.violationReported);
        assertTrue(rule.violationDetails.contains("user_xyz_789"));
    }
    
    @Test
    void shouldNotTriggerOnUnrelatedUrl() throws Exception {
        // Capture Mode
        when(respHeader.isEmpty()).thenReturn(false);
        when(respHeader.getHeaderValues(HttpHeader.SET_COOKIE)).thenReturn(List.of("secret=super_secret_value"));
        
        rule.scan(msg);
        
        // Check Mode with unrelated URL
        when(respHeader.isEmpty()).thenReturn(true);
        when(reqHeader.getURI()).thenReturn(new URI("http://example.com/public", true));
        
        rule.scan(msg);
        
        assertFalse(rule.violationReported);
    }

    // Subclass to capture notifications
    private static class TestableCookieSyncRule extends CookieSyncRule {
        boolean violationReported = false;
        String violationDetails = "";

        @Override
        protected void notifyViolation(HttpMessage msg, String details) {
            violationReported = true;
            violationDetails = details;
        }
    }
}

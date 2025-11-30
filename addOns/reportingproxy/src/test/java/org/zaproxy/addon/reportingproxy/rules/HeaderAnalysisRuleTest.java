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

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.parosproxy.paros.network.HttpHeaderField;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpRequestHeader;
import org.parosproxy.paros.network.HttpResponseHeader;

class HeaderAnalysisRuleTest {

    private TestableHeaderAnalysisRule rule;
    private HttpMessage msg;
    private HttpRequestHeader reqHeader;
    private HttpResponseHeader respHeader;

    @BeforeEach
    void setUp() {
        rule = new TestableHeaderAnalysisRule();
        msg = mock(HttpMessage.class);
        reqHeader = mock(HttpRequestHeader.class);
        respHeader = mock(HttpResponseHeader.class);
        when(msg.getRequestHeader()).thenReturn(reqHeader);
        when(msg.getResponseHeader()).thenReturn(respHeader);
        when(reqHeader.getHostName()).thenReturn("example.com");
    }

    @Test
    void shouldNotMatchInitially() {
        when(respHeader.isEmpty()).thenReturn(false);
        when(respHeader.getHeaders()).thenReturn(List.of(new HttpHeaderField("Strict-Transport-Security", "max-age=31536000")));
        
        rule.scan(msg);
        assertFalse(rule.violationReported);
    }

    @Test
    void shouldMatchWhenCommonHeaderMissing() {
        when(respHeader.isEmpty()).thenReturn(false);
        
        // 5 responses with HSTS
        for (int i = 0; i < 5; i++) {
            when(respHeader.getHeaders()).thenReturn(List.of(new HttpHeaderField("Strict-Transport-Security", "max-age=31536000")));
            rule.scan(msg);
        }
        
        // 6th response without HSTS
        when(respHeader.getHeaders()).thenReturn(Collections.emptyList());
        rule.scan(msg);
        
        assertTrue(rule.violationReported);
        assertTrue(rule.violationDetails.contains("Strict-Transport-Security"));
    }
    
    @Test
    void shouldNotMatchWhenCommonHeaderPresent() {
        when(respHeader.isEmpty()).thenReturn(false);
        
        // 5 responses with HSTS
        for (int i = 0; i < 5; i++) {
            when(respHeader.getHeaders()).thenReturn(List.of(new HttpHeaderField("Strict-Transport-Security", "max-age=31536000")));
            rule.scan(msg);
        }
        
        // 6th response with HSTS
        when(respHeader.getHeaders()).thenReturn(List.of(new HttpHeaderField("Strict-Transport-Security", "max-age=31536000")));
        rule.scan(msg);
        
        assertFalse(rule.violationReported);
    }

    // Subclass to capture notifications without needing the singleton
    private static class TestableHeaderAnalysisRule extends HeaderAnalysisRule {
        boolean violationReported = false;
        String violationDetails = "";

        @Override
        protected void notifyViolation(HttpMessage msg, String details) {
            violationReported = true;
            violationDetails = details;
        }
    }
}

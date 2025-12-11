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
package org.zaproxy.addon.reportingproxy;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CopyOnWriteArrayList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.network.HttpMessage;

/**
 * Controls scanning logic and manages list of rules.
 *
 * It receives HTTP messages from the
 * {@link ReportingProxyListener} and delivers them to each registered {@link ReportingRule}.
 */
public class ReportingProxyController {

    private static final Logger LOGGER = LogManager.getLogger(ReportingProxyController.class);
    private final RuleManager ruleManager;
    private final RuleLoader ruleLoader;

    public ReportingProxyController() {
        this.ruleManager = new RuleManager();
        this.ruleLoader = new RuleLoader();
    }

    /**
     * Adds a new rule to the active set.
     * 
     * @param rule The rule to add.
     */
    public void addRule(ReportingRule rule) {
        ruleManager.addRule(rule);
    }

    /**
     * Removes a rule from the active set.
     * 
     * @param rule The rule to remove.
     */
    public void removeRule(ReportingRule rule) {
        ruleManager.removeRule(rule);
    }

    /**
     * Clears all active rules.
     */
    public void clearRules() {
        ruleManager.clearRules();
    }

    /**
     * Gets the rule loader used for loading rules from JARs.
     *
     * @return The {@link RuleLoader}.
     */
    public RuleLoader getRuleLoader() {
        return ruleLoader;
    }

    /**
     * Scans the given HTTP message against all active rules.
     *
     * If a rule throws an exception during scanning, it is logged, and the scan continues with the
     * next rule.
     *
     * @param msg The HTTP message to scan.
     */
    public void scan(HttpMessage msg) {
        for (ReportingRule rule : ruleManager.getRules()) {
            try {
                rule.scan(msg);
            } catch (BlockingViolationException e) {
                LOGGER.info("Blocking violation detected: {}", e.getDetails());
                handleBlocking(msg, e);
                break;
            } catch (Exception e) {
                LOGGER.error("Error scanning message with rule {}: {}", rule.getName(), e.getMessage(), e);
            }
        }
    }

    private void handleBlocking(HttpMessage msg, BlockingViolationException e) {
        try {
            e.getRule().incrementBlockedCount();

            boolean isJson = false;
            String contentType = msg.getRequestHeader().getHeader("Content-Type");
            if (contentType != null && contentType.contains("application/json")) {
                isJson = true;
            }
            
            if (isJson) {
                msg.setResponseHeader(
                        "HTTP/1.1 429 Too Many Requests\r\n" +
                        "Content-Type: application/json\r\n" +
                        "Content-Length: 0\r\n");
                msg.setResponseBody("{\"error\": \"Blocked by Filtering Proxy\", \"reason\": \"" + e.getDetails() + "\"}");
            } else {
                String html = loadBlockedHtmlTemplate();
                html = html.replace("{{DETAILS}}", e.getDetails());
                
                msg.setResponseHeader(
                        "HTTP/1.1 200 OK\r\n" +
                        "Content-Type: text/html\r\n" +
                        "Content-Length: " + html.length() + "\r\n");
                msg.setResponseBody(html);
            }
            
            msg.getResponseHeader().setContentLength(msg.getResponseBody().length());
            
        } catch (Exception ex) {
            LOGGER.error("Error handling blocking response", ex);
        }
    }

    private String loadBlockedHtmlTemplate() {
        try (InputStream is = getClass().getResourceAsStream("resources/blocked.html")) {
            if (is == null) {
                return "<html><body><h1>Request Blocked</h1><p>{{DETAILS}}</p></body></html>";
            }
            try (Scanner scanner = new Scanner(is, StandardCharsets.UTF_8.name())) {
                return scanner.useDelimiter("\\A").next();
            }
        } catch (IOException e) {
            LOGGER.error("Error loading blocked.html template", e);
            return "<html><body><h1>Request Blocked</h1><p>{{DETAILS}}</p></body></html>";
        }
    }
    
    /**
     * Gets the list of active rules.
     *
     * @return The list of {@link ReportingRule}s.
     */
    public List<ReportingRule> getRules() {
        return ruleManager.getRules();
    }
}

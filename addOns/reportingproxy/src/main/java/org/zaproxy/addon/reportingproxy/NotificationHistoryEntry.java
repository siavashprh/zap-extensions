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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents a single notification history entry.
 */
public class NotificationHistoryEntry {
    
    private final String ruleName;
    private final String url;
    private final String details;
    private final LocalDateTime timestamp;
    
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * Creates a new notification history entry.
     * 
     * @param ruleName The name of the rule that was triggered.
     * @param url The URL where the violation occurred.
     * @param details Additional details about the violation.
     */
    public NotificationHistoryEntry(String ruleName, String url, String details) {
        this.ruleName = ruleName;
        this.url = url;
        this.details = details;
        this.timestamp = LocalDateTime.now();
    }
    
    /**
     * Gets the rule name.
     * 
     * @return The rule name.
     */
    public String getRuleName() {
        return ruleName;
    }
    
    /**
     * Gets the URL.
     * 
     * @return The URL.
     */
    public String getUrl() {
        return url;
    }
    
    /**
     * Gets the details.
     * 
     * @return The details.
     */
    public String getDetails() {
        return details;
    }
    
    /**
     * Gets the timestamp.
     * 
     * @return The timestamp.
     */
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    /**
     * Gets the formatted timestamp string.
     * 
     * @return The formatted timestamp.
     */
    public String getFormattedTimestamp() {
        return timestamp.format(FORMATTER);
    }
}

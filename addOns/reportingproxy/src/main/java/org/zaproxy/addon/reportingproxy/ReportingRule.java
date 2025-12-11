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

import org.parosproxy.paros.network.HttpMessage;

/**
 * Abstract base class for reporting rules.
 * Handles the state for blocking configuration and statistics.
 */
public abstract class ReportingRule {

    private boolean blocking = false;
    private int blockedCount = 0;
    private NotificationService notificationService;

    /**
     * Scans the given message and triggers a notification if the rule is violated.
     *
     * @param msg The HTTP message to scan.
     */
    public abstract void scan(HttpMessage msg);

    /**
     * Gets the name of the rule.
     *
     * @return The name of the rule.
     */
    public abstract String getName();

    /**
     * Gets the description of the rule.
     *
     * @return The description of the rule.
     */
    public abstract String getDescription();

    /**
     * Sets the notification service to be used by the rule.
     * 
     * @param service The notification service.
     */
    public void setNotificationService(NotificationService service) {
        this.notificationService = service;
    }
    
    protected NotificationService getNotificationService() {
        return notificationService;
    }

    /**
     * Checks if the rule is currently set to blocking mode.
     *
     * @return true if the rule is blocking, false otherwise.
     */
    public boolean isBlocking() {
        return blocking;
    }

    /**
     * Sets the blocking mode for the rule.
     *
     * @param blocking true to enable blocking, false to disable.
     */
    public void setBlocking(boolean blocking) {
        this.blocking = blocking;
    }

    /**
     * Gets the number of times this rule has blocked a request.
     *
     * @return The number of blocked requests.
     */
    public int getBlockedCount() {
        return blockedCount;
    }

    /**
     * Increments the blocked count.
     */
    public void incrementBlockedCount() {
        this.blockedCount++;
    }
}

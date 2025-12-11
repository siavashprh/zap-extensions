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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.view.View;
import org.zaproxy.addon.reportingproxy.ui.NotificationManager;

/**
 * Handles notifications for rule violations.
 * 
 * @param rule The rule that triggered the notification.
 * @param msg The HTTP message that triggered the notification.
 * @param details Additional details about the notification.
 */
public class NotificationService {

    /** The logger for the notification service. */
    private static final Logger LOGGER = LogManager.getLogger(NotificationService.class);
    /** The singleton instance of the notification service. */
    private static NotificationService instance;

    private NotificationService() {}

    /**
     * Gets the singleton instance of the notification service.
     * 
     * @return The singleton instance of the notification service.
     */
    public static synchronized NotificationService getSingleton() {
        if (instance == null) {
            instance = new NotificationService();
        }
        return instance;
    }

    /**
     * Notifies the user of a rule violation.
     * 
     * Shows both a pop-up notification window and logs to the output panel.
     * If the rule is blocking, it throws a BlockingViolationException.
     * 
     * @param rule The rule that triggered the notification.
     * @param msg The HTTP message that triggered the notification.
     * @param details Additional details about the notification.
     */
    public void notify(ReportingRule rule, HttpMessage msg, String details) {
        String url = "Unknown URL";
        try {
            if (msg != null && msg.getRequestHeader() != null && msg.getRequestHeader().getURI() != null) {
                url = msg.getRequestHeader().getURI().toString();
            }
        } catch (Exception e) {
            LOGGER.debug("Error getting URL from message: {}", e.getMessage());
        }
        
        if (View.isInitialised()) {
            NotificationManager notificationManager = NotificationManager.getInstance();
            String displayDetails = details;
            if (rule.isBlocking()) {
                displayDetails = "[BLOCKED] " + details;
            }
            notificationManager.showNotification(rule.getName(), url, displayDetails);
            
            String notification =
                    String.format(
                            "[Reporting Proxy] Rule '%s' triggered. URL: %s. Details: %s\n",
                            rule.getName(), url, displayDetails);
            javax.swing.SwingUtilities.invokeLater(() -> 
                View.getSingleton().getOutputPanel().append(notification)
            );
        } else {
            LOGGER.info("[Reporting Proxy] Rule '{}' triggered. URL: {}. Details: {}", 
                    rule.getName(), url, details);
        }

        if (rule.isBlocking()) {
            throw new BlockingViolationException(rule, details);
        }
    }
}
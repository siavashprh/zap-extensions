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
import org.parosproxy.paros.view.View;

/**
 * Handles notifications for rule violations.
 * 
 * 
 */
public class NotificationService {

    private static NotificationService instance;

    private NotificationService() {}

    public static synchronized NotificationService getSingleton() {
        if (instance == null) {
            instance = new NotificationService();
        }
        return instance;
    }

    public void notify(ReportingRule rule, HttpMessage msg, String details) {
        if (View.isInitialised()) {
            String notification =
                    String.format(
                            "[Reporting Proxy] Rule '%s' triggered. URL: %s. Details: %s\n",
                            rule.getName(), msg.getRequestHeader().getURI().toString(), details);
            javax.swing.SwingUtilities.invokeLater(() -> 
                View.getSingleton().getOutputPanel().append(notification)
            );
        } else {
            System.out.println("[Reporting Proxy] " + rule.getName() + ": " + details);
        }
    }
}

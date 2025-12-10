/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2024 The ZAP Development Team
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
 * Interface for a reporting rule. Rules can be stateless (checking a single message) or stateful
 * (maintaining history).
 */
public interface ReportingRule {

    /**
     * Scans the given message and triggers a notification if the rule is violated.
     *
     * @param msg The HTTP message to scan.
     */
    void scan(HttpMessage msg);

    /**
     * Gets the name of the rule.
     *
     * @return The name of the rule.
     */
    String getName();

    /**
     * Gets the description of the rule.
     *
     * @return The description of the rule.
     */
    String getDescription();

    /**
     * Checks if the rule is currently set to blocking mode.
     *
     * @return true if the rule is blocking, false otherwise.
     */
    default boolean isBlocking() {
        return false;
    }

    /**
     * Sets the blocking mode for the rule.
     *
     * @param blocking true to enable blocking, false to disable.
     */
    default void setBlocking(boolean blocking) {
        // Default implementation does nothing
    }

    /**
     * Gets the number of times this rule has blocked a request.
     *
     * @return The number of blocked requests.
     */
    default int getBlockedCount() {
        return 0;
    }

    /**
     * Increments the blocked count.
     */
    default void incrementBlockedCount() {
        // Default implementation does nothing
    }
}

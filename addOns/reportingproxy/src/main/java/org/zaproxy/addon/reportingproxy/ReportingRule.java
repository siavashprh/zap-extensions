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
 * Interface for rules that check HTTP messages for violations.
 */
public interface ReportingRule {

    /**
     * Gets the name of the rule.
     *
     * @return the name of the rule.
     */
    String getName();

    /**
     * Checks the given message for violations.
     *
     * @param msg the message to check.
     * @return a violation string if the rule is broken, or null if the request is safe.
     */
    String check(HttpMessage msg);
}

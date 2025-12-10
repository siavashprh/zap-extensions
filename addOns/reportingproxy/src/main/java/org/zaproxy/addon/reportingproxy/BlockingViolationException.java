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

/**
 * Exception thrown when a blocking rule is violated.
 * This signals the controller to intervene and block the traffic.
 */
public class BlockingViolationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final transient ReportingRule rule;
    private final String details;

    /**
     * Constructs a new BlockingViolationException.
     * 
     * @param rule The rule that was violated.
     * @param details Details about the violation.
     */
    public BlockingViolationException(ReportingRule rule, String details) {
        super(details);
        this.rule = rule;
        this.details = details;
    }

    /**
     * Gets the rule that was violated.
     * 
     * @return The violated rule.
     */
    public ReportingRule getRule() {
        return rule;
    }

    /**
     * Gets the details of the violation.
     * 
     * @return The violation details.
     */
    public String getDetails() {
        return details;
    }
}

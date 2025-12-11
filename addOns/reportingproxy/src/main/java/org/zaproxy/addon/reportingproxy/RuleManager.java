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

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Manages the storage of reporting rules.
 */
public class RuleManager {

    private static final Logger LOGGER = LogManager.getLogger(RuleManager.class);
    private final List<ReportingRule> rules = new CopyOnWriteArrayList<>();

    /**
     * Adds a new rule to the active set.
     * 
     * @param rule The rule to add.
     */
    public void addRule(ReportingRule rule) {
        for (ReportingRule existingRule : rules) {
            if (existingRule.getName().equals(rule.getName())) {
                LOGGER.warn("Rule with name '{}' already exists. Skipping addition.", rule.getName());
                return;
            }
        }
        this.rules.add(rule);
    }

    /**
     * Removes a rule from the active set.
     * 
     * @param rule The rule to remove.
     */
    public void removeRule(ReportingRule rule) {
        this.rules.remove(rule);
    }

    /**
     * Clears all active rules.
     */
    public void clearRules() {
        this.rules.clear();
    }

    /**
     * Gets the list of active rules.
     * 
     * @return An unmodifiable list of active rules.
     */
    public List<ReportingRule> getRules() {
        return Collections.unmodifiableList(rules);
    }
}

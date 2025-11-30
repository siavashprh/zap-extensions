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

import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpSender;
import org.zaproxy.zap.network.HttpSenderListener;

/** Listener that intercepts HTTP traffic and checks it against configured reporting rules. */
public class ReportingListener implements HttpSenderListener {

    private static final Logger LOGGER = LogManager.getLogger(ReportingListener.class);

    private final List<ReportingRule> rules;

    public ReportingListener() {
        this.rules = new ArrayList<>();
        // Hardcoded rule for now
        this.rules.add(new RateLimitRule());
        LOGGER.info("ReportingListener initialized with {} rule(s)", rules.size());
    }

    @Override
    public int getListenerOrder() {
        // Run late in the chain
        return 9000;
    }

    @Override
    public void onHttpRequestSend(HttpMessage msg, int initiator, HttpSender sender) {
        // Check each rule
        for (ReportingRule rule : rules) {
            String violation = rule.check(msg);
            if (violation != null) {
                // Log violation using ZAP's logging framework
                LOGGER.warn("[REPORTING PROXY VIOLATION] {}", violation);
            }
        }
    }

    @Override
    public void onHttpResponseReceive(HttpMessage msg, int initiator, HttpSender sender) {
        // TODO: implementation
    }
}

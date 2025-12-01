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

import java.util.ArrayList;
import java.util.List;
import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.extension.ExtensionHook;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpSender;
import org.zaproxy.zap.network.HttpSenderListener;

public class ExtensionReportingProxy extends ExtensionAdaptor implements HttpSenderListener {

    public static final String NAME = "ExtensionReportingProxy";
    private List<ReportingRule> rules = new ArrayList<>();
    private RuleLoader ruleLoader = new RuleLoader();
    private ReportingProxyPanel panel;

    public ExtensionReportingProxy() {
        super(NAME);
        setI18nPrefix("reportingproxy");
    }

    @Override
    public void hook(ExtensionHook extensionHook) {
        super.hook(extensionHook);
        extensionHook.addHttpSenderListener(this);

        // Load default rules
        rules.add(new org.zaproxy.addon.reportingproxy.rules.RateLimitRule());
        rules.add(new org.zaproxy.addon.reportingproxy.rules.HeaderAnalysisRule());
        rules.add(new org.zaproxy.addon.reportingproxy.rules.CookieSyncRule());

        if (getView() != null) {
            extensionHook.getHookView().addStatusPanel(getReportingProxyPanel());
        }
    }

    @Override
    public boolean canUnload() {
        return true;
    }

    @Override
    public void unload() {
        super.unload();
    }

    @Override
    public int getListenerOrder() {
        return 9000; // High order to run late
    }

    @Override
    public void onHttpRequestSend(HttpMessage msg, int initiator, HttpSender helper) {
        for (ReportingRule rule : rules) {
            try {
                rule.scan(msg);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onHttpResponseReceive(HttpMessage msg, int initiator, HttpSender helper) {
        for (ReportingRule rule : rules) {
            try {
                rule.scan(msg);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void addRule(ReportingRule rule) {
        this.rules.add(rule);
    }

    public void clearRules() {
        this.rules.clear();
    }

    public RuleLoader getRuleLoader() {
        return ruleLoader;
    }

    private ReportingProxyPanel getReportingProxyPanel() {
        if (panel == null) {
            panel = new ReportingProxyPanel(this);
        }
        return panel;
    }
}

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

import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.extension.ExtensionHook;

public class ExtensionReportingProxy extends ExtensionAdaptor {

    public static final String NAME = "ExtensionReportingProxy";
    private ReportingProxyController controller;
    private ReportingProxyListener listener;
    private ReportingProxyPanel panel;

    public ExtensionReportingProxy() {
        super(NAME);
        setI18nPrefix("reportingproxy");
    }

    @Override
    public void hook(ExtensionHook extensionHook) {
        super.hook(extensionHook);
        
        controller = new ReportingProxyController();
        listener = new ReportingProxyListener(controller);
        
        extensionHook.addHttpSenderListener(listener);

        // Load default rules
        // Commented out for testing JAR loading - uncomment to make it default again
        // controller.addRule(new org.zaproxy.addon.reportingproxy.rules.RateLimitRule());
        controller.addRule(new org.zaproxy.addon.reportingproxy.rules.HeaderAnalysisRule());
        controller.addRule(new org.zaproxy.addon.reportingproxy.rules.CookieSyncRule());
        controller.addRule(new org.zaproxy.addon.reportingproxy.rules.CspDetectionRule());

        if (getView() != null) {
            extensionHook.getHookView().addStatusPanel(getReportingProxyPanel());
        }
    }

    @Override
    public boolean canUnload() {
        return true;
    }

    public ReportingProxyController getController() {
        return controller;
    }

    private ReportingProxyPanel getReportingProxyPanel() {
        if (panel == null) {
            panel = new ReportingProxyPanel(this);
        }
        return panel;
    }
}

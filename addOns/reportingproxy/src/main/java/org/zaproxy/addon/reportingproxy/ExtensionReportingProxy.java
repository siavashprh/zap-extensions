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

/**
 * The extension that provides reporting proxy functionality.
 * 
 * @param extensionHook The extension hook to use for adding listeners.
 */
public class ExtensionReportingProxy extends ExtensionAdaptor {

    /** The name of the extension. */
    public static final String NAME = "ExtensionReportingProxy";
    /** The controller for the extension. */
    private ReportingProxyController controller;
    /** The listener for the extension. */
    private ReportingProxyListener listener;
    /** The panel for the extension within ZAP UI */
    private ReportingProxyPanel panel;

    /** Constructor for the extension. */
    public ExtensionReportingProxy() {
        super(NAME);
        setI18nPrefix("reportingproxy");
    }

    /**
     * Hooks the extension into ZAP.
     * 
     * @param extensionHook The extension hook to use for adding listeners.
     */
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

    /**
     * @return true if the extension can be unloaded, false otherwise.
     */
    @Override
    public boolean canUnload() {
        return true;
    }

    /**
     * @return the controller for the extension.
     */
    public ReportingProxyController getController() {
        return controller;
    }

    /**
     * @return the panel for the extension within ZAP UI.
     */
    private ReportingProxyPanel getReportingProxyPanel() {
        if (panel == null) {
            panel = new ReportingProxyPanel(this);
        }
        return panel;
    }
}

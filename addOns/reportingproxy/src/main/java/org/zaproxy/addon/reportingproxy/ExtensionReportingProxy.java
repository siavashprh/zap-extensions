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

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.extension.ExtensionHook;
import org.zaproxy.addon.reportingproxy.ui.ReportingProxyPanel;

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
    /** Logger for this class. */
    private static final Logger LOGGER = LogManager.getLogger(ExtensionReportingProxy.class);

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

        loadDefaultRulesFromJars();

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

    /**
     * Loads default rules from JAR files included in the add-on package.
     * Falls back to direct instantiation if JAR files are not available (e.g., during development).
     */
    private void loadDefaultRulesFromJars() {
        if (getAddOn() == null) {
            LOGGER.debug("Add-on not available, loading rules directly");
            loadRulesDirectly();
            return;
        }

        List<String> addOnFiles = getAddOn().getFiles();
        if (addOnFiles == null || addOnFiles.isEmpty()) {
            LOGGER.debug("No add-on files found, loading rules directly");
            loadRulesDirectly();
            return;
        }

        Path zapHome = Paths.get(Constant.getZapHome());
        int loadedCount = 0;

        for (String file : addOnFiles) {
            if (file != null && file.endsWith(".jar") && file.contains("rule")) {
                try {
                    Path jarPath = zapHome.resolve(file);
                    File jarFile = jarPath.toFile();
                    
                    if (jarFile.exists()) {
                        LOGGER.debug("Loading rules from JAR: {}", jarFile.getName());
                        List<ReportingRule> rules = controller.getRuleLoader().loadRules(jarFile);
                        for (ReportingRule rule : rules) {
                            controller.addRule(rule);
                            loadedCount++;
                        }
                    } else {
                        LOGGER.warn("Rule JAR file not found: {}", jarPath);
                    }
                } catch (Exception e) {
                    LOGGER.error("Error loading rules from JAR file {}: {}", file, e.getMessage(), e);
                }
            }
        }

        if (loadedCount == 0) {
            LOGGER.debug("No rules loaded from JARs, falling back to direct instantiation");
            loadRulesDirectly();
        } else {
            LOGGER.info("Loaded {} default rules from JAR files", loadedCount);
        }
    }

    /**
     * Loads default rules by directly instantiating them.
     * Used as a fallback when JAR files are not available.
     */
    private void loadRulesDirectly() {
        try {
            controller.addRule(new org.zaproxy.addon.reportingproxy.rules.HeaderAnalysisRule());
            controller.addRule(new org.zaproxy.addon.reportingproxy.rules.CookieSyncRule());
            controller.addRule(new org.zaproxy.addon.reportingproxy.rules.CspDetectionRule());
            LOGGER.debug("Loaded default rules directly");
        } catch (Exception e) {
            LOGGER.error("Error loading default rules directly: {}", e.getMessage(), e);
        }
    }
}
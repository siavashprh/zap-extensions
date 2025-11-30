/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2014 The ZAP Development Team
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

import java.util.List;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.extension.ExtensionHook;
import org.parosproxy.paros.network.HttpSender;

/**
 * A headless ZAP extension for the Reporting Proxy.
 *
 * <p>{@link ExtensionAdaptor} classes are the main entry point for adding/loading functionalities
 * provided by the add-ons.
 *
 * @see #hook(ExtensionHook)
 */
public class ExtensionReportingProxy extends ExtensionAdaptor {

    // The name is public so that other extensions can access it
    public static final String NAME = "ExtensionReportingProxy";

    // The i18n prefix, by default the package name - defined in one place to make it easier
    // to copy and change this example
    protected static final String PREFIX = "reportingproxy";

    private ReportingListener listener;

    private List<ReportingRule> rules;
    private RuleLoader ruleLoader = new RuleLoader();

    public ExtensionReportingProxy() {
        super(NAME);
        setI18nPrefix(PREFIX);
    }

    @Override
    public void hook(ExtensionHook extensionHook) {
        super.hook(extensionHook);

        this.listener = new ReportingListener();
        HttpSender.addListener(this.listener);
    }

    @Override
    public boolean canUnload() {
        // The extension can be dynamically unloaded, all resources used/added can be freed/removed
        // from core.
        return true;
    }

    @Override
    public void unload() {
        super.unload();

        if (this.listener != null) {
            HttpSender.removeListener(this.listener);
        }
    }

    @Override
    public String getDescription() {
        return Constant.messages.getString(PREFIX + ".desc");
    }
}

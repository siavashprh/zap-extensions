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

import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpSender;
import org.zaproxy.zap.network.HttpSenderListener;

/**
 * Listens to all HTTP requests and responses passing through ZAP.
 *
 * Intercepting traffic and forwarding it to the {@link ReportingProxyController} for analysis.
 * It is registered with a high listener order to ensure it sees traffic after other modifications.
 */
public class ReportingProxyListener implements HttpSenderListener {

    private ReportingProxyController controller;

    /**
     * Constructs a new listener.
     *
     * @param controller The controller to forward messages to.
     */
    public ReportingProxyListener(ReportingProxyController controller) {
        this.controller = controller;
    }

    /**
     * @return the listener order.
     */
    @Override
    public int getListenerOrder() {
        return 9000;
    }

    /**
     * @param msg The HTTP message to scan.
     * @param initiator The initiator of the message.
     * @param helper The helper to use for sending the message.
     */
    @Override
    public void onHttpRequestSend(HttpMessage msg, int initiator, HttpSender helper) {
        controller.scan(msg);
    }

    /**
     * @param msg The HTTP message to scan.
     * @param initiator The initiator of the message.
     * @param helper The helper to use for sending the message.
     */
    @Override
    public void onHttpResponseReceive(HttpMessage msg, int initiator, HttpSender helper) {
        controller.scan(msg);
    }
}

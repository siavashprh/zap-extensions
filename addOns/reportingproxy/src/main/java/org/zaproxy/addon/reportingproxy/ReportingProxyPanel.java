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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.parosproxy.paros.extension.AbstractPanel;
import org.parosproxy.paros.view.View;

@SuppressWarnings("serial")
public class ReportingProxyPanel extends AbstractPanel {

    private ExtensionReportingProxy extension;
    private JLabel statusLabel;

    public ReportingProxyPanel(ExtensionReportingProxy extension) {
        super();
        this.extension = extension;
        this.setLayout(new GridBagLayout());
        this.setName("Reporting Proxy");

        JButton loadButton = new JButton("Load Rules JAR");
        loadButton.addActionListener(
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        JFileChooser fileChooser = new JFileChooser();
                        fileChooser.setFileFilter(new FileNameExtensionFilter("JAR Files", "jar"));
                        int result = fileChooser.showOpenDialog(View.getSingleton().getMainFrame());
                        if (result == JFileChooser.APPROVE_OPTION) {
                            File selectedFile = fileChooser.getSelectedFile();
                            loadRules(selectedFile);
                        }
                    }
                });

        statusLabel = new JLabel("No external rules loaded.");

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        this.add(loadButton, gbc);

        gbc.gridy = 1;
        this.add(statusLabel, gbc);
    }

    private void loadRules(File jarFile) {
        try {
            java.util.List<ReportingRule> newRules = extension.getRuleLoader().loadRules(jarFile);
            for (ReportingRule rule : newRules) {
                extension.addRule(rule);
            }
            statusLabel.setText("Loaded " + newRules.size() + " rules from " + jarFile.getName());
            JOptionPane.showMessageDialog(
                    View.getSingleton().getMainFrame(),
                    "Successfully loaded " + newRules.size() + " rules.",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(
                    View.getSingleton().getMainFrame(),
                    "Error loading rules: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}

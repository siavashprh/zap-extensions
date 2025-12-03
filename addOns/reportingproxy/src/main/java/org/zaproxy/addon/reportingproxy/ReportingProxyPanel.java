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

import java.awt.BorderLayout;
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
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.extension.AbstractPanel;
import org.parosproxy.paros.view.View;

/**
 * Reporting proxy panel.
 * 
 * This panel is used to load external rules and display them in a table.
 */
@SuppressWarnings("serial")
public class ReportingProxyPanel extends AbstractPanel {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LogManager.getLogger(ReportingProxyPanel.class);

    private transient ExtensionReportingProxy extension;
    private JLabel statusLabel;
    private JTable rulesTable;
    private DefaultTableModel tableModel;

    /**
     * Constructs a new ReportingProxyPanel.
     * 
     * @param extension The extension that owns this panel.
     */
    public ReportingProxyPanel(ExtensionReportingProxy extension) {
        super();
        this.extension = extension;
        this.setLayout(new BorderLayout());
        this.setName("Reporting Proxy");
        this.setIcon(new javax.swing.ImageIcon(ReportingProxyPanel.class.getResource("/org/zaproxy/addon/reportingproxy/resources/icon.png")));

        // Create top panel with button and status
        JPanel topPanel = new JPanel(new GridBagLayout());
        
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
        topPanel.add(loadButton, gbc);

        gbc.gridy = 1;
        topPanel.add(statusLabel, gbc);

        this.add(topPanel, BorderLayout.NORTH);

        // Create table to display active rules
        String[] columnNames = {"Rule Name", "Description"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table read-only
            }
        };
        rulesTable = new JTable(tableModel);
        rulesTable.getColumnModel().getColumn(0).setPreferredWidth(200);
        rulesTable.getColumnModel().getColumn(1).setPreferredWidth(400);
        
        JScrollPane scrollPane = new JScrollPane(rulesTable);
        this.add(scrollPane, BorderLayout.CENTER);

        // Populate table with default rules
        javax.swing.SwingUtilities.invokeLater(() -> refreshRulesTable());
    }

    private void refreshRulesTable() {
        tableModel.setRowCount(0); // Clear existing rows
        
        if (extension.getController() != null) {
            for (ReportingRule rule : extension.getController().getRules()) {
                tableModel.addRow(new Object[]{
                    rule.getName(),
                    rule.getDescription()
                });
            }
        }
    }

    /**
     * Loads rules from a JAR file.
     * 
     * @param jarFile The JAR file to load rules from.
     */
    private void loadRules(File jarFile) {
        try {
            java.util.List<ReportingRule> newRules = extension.getController().getRuleLoader().loadRules(jarFile);
            for (ReportingRule rule : newRules) {
                extension.getController().addRule(rule);
            }
            statusLabel.setText("Loaded " + newRules.size() + " rules from " + jarFile.getName());
            
            // Refresh the table to show new rules
            refreshRulesTable();
            
            JOptionPane.showMessageDialog(
                    View.getSingleton().getMainFrame(),
                    "Successfully loaded " + newRules.size() + " rules.",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            LOGGER.error("Error loading rules from JAR {}: {}", jarFile.getName(), e.getMessage(), e);
            JOptionPane.showMessageDialog(
                    View.getSingleton().getMainFrame(),
                    "Error loading rules: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}

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
package org.zaproxy.addon.reportingproxy.ui;

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
import org.zaproxy.addon.reportingproxy.ExtensionReportingProxy;
import org.zaproxy.addon.reportingproxy.ReportingRule;
import org.zaproxy.zap.utils.DisplayUtils;

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
        this.setIcon(DisplayUtils.getScaledIcon(ReportingProxyPanel.class.getResource("/org/zaproxy/addon/reportingproxy/resources/icon.png")));

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

        JButton removeButton = new JButton("Remove Rule");
        removeButton.setEnabled(false); // Disabled until row selected
        removeButton.addActionListener(e -> removeSelectedRule());
        
        gbc.gridx = 1;
        topPanel.add(removeButton, gbc);
        
        JButton historyButton = new JButton("View Notification History");
        historyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                NotificationManager manager = NotificationManager.getInstance();
                NotificationHistoryDialog dialog = new NotificationHistoryDialog(
                    View.getSingleton().getMainFrame(), 
                    manager
                );
                dialog.setVisible(true);
            }
        });
        
        gbc.gridx = 2;
        topPanel.add(historyButton, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        topPanel.add(statusLabel, gbc);
        
        gbc.gridwidth = 1;

        this.add(topPanel, BorderLayout.NORTH);

        // Create table to display active rules
        String[] columnNames = {"Rule Name", "Description", "Blocking", "Blocked Count"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 2; 
            }
            
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 2) {
                    return Boolean.class;
                }
                return super.getColumnClass(columnIndex);
            }
        };
        rulesTable = new JTable(tableModel);
        rulesTable.getColumnModel().getColumn(0).setPreferredWidth(200);
        rulesTable.getColumnModel().getColumn(1).setPreferredWidth(400);
        rulesTable.getColumnModel().getColumn(2).setPreferredWidth(80);
        rulesTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        
        tableModel.addTableModelListener(e -> {
            if (e.getType() == javax.swing.event.TableModelEvent.UPDATE && e.getColumn() == 2) {
                int row = e.getFirstRow();
                boolean isBlocking = (Boolean) tableModel.getValueAt(row, 2);
                String ruleName = (String) tableModel.getValueAt(row, 0);
                
                for (ReportingRule rule : extension.getController().getRules()) {
                    if (rule.getName().equals(ruleName)) {
                        rule.setBlocking(isBlocking);
                        break;
                    }
                }
            }
        });
        
        rulesTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                removeButton.setEnabled(rulesTable.getSelectedRow() != -1);
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(rulesTable);
        this.add(scrollPane, BorderLayout.CENTER);

        javax.swing.SwingUtilities.invokeLater(() -> refreshRulesTable());
    }

    private void refreshRulesTable() {
        tableModel.setRowCount(0); 
        
        if (extension.getController() != null) {
            for (ReportingRule rule : extension.getController().getRules()) {
                tableModel.addRow(new Object[]{
                    rule.getName(),
                    rule.getDescription(),
                    rule.isBlocking(),
                    rule.getBlockedCount()
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

    private void removeSelectedRule() {
        int selectedRow = rulesTable.getSelectedRow();
        if (selectedRow != -1) {
            String ruleName = (String) tableModel.getValueAt(selectedRow, 0);
            
            // Find the rule object
            ReportingRule ruleToRemove = null;
            for (ReportingRule rule : extension.getController().getRules()) {
                if (rule.getName().equals(ruleName)) {
                    ruleToRemove = rule;
                    break;
                }
            }
            
            if (ruleToRemove != null) {
                extension.getController().removeRule(ruleToRemove);
                refreshRulesTable();
                statusLabel.setText("Removed rule: " + ruleName);
            }
        }
    }
}

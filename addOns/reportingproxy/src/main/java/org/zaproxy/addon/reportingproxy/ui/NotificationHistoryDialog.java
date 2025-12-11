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
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import org.zaproxy.zap.utils.DisplayUtils;

/**
 * Dialog that displays the history of all notifications.
 */
@SuppressWarnings("serial")
public class NotificationHistoryDialog extends JDialog {

    private static final long serialVersionUID = 1L;
    
    private JTable historyTable;
    private DefaultTableModel tableModel;
    private NotificationManager notificationManager;
    
    /**
     * Creates a new notification history dialog.
     * 
     * @param owner The owner frame.
     * @param notificationManager The notification manager to get history from.
     */
    public NotificationHistoryDialog(Frame owner, NotificationManager notificationManager) {
        super(owner, "Notification History", true);
        this.notificationManager = notificationManager;
        
        setLayout(new BorderLayout());
        setSize(DisplayUtils.getScaledDimension(800, 500));
        setLocationRelativeTo(owner);
        
        String[] columnNames = {"Timestamp", "Rule Name", "URL", "Details"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table read-only
            }
        };
        historyTable = new JTable(tableModel);
        historyTable.getColumnModel().getColumn(0).setPreferredWidth(150);
        historyTable.getColumnModel().getColumn(1).setPreferredWidth(200);
        historyTable.getColumnModel().getColumn(2).setPreferredWidth(250);
        historyTable.getColumnModel().getColumn(3).setPreferredWidth(200);
        
        JScrollPane scrollPane = new JScrollPane(historyTable);
        add(scrollPane, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        
        JButton clearButton = new JButton("Clear History");
        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                notificationManager.clearHistory();
                refreshTable();
            }
        });
        
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refreshTable();
            }
        });
        
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.WEST;
        buttonPanel.add(clearButton, gbc);
        
        gbc.gridx = 1;
        buttonPanel.add(refreshButton, gbc);
        
        gbc.gridx = 2;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        buttonPanel.add(new JPanel(), gbc); // Spacer
        
        gbc.gridx = 3;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.EAST;
        buttonPanel.add(closeButton, gbc);
        
        add(buttonPanel, BorderLayout.SOUTH);
        
        refreshTable();
    }
    
    /**
     * Refreshes the table with current history data.
     */
    private void refreshTable() {
        tableModel.setRowCount(0); // Clear existing rows
        
        List<NotificationHistoryEntry> history = notificationManager.getNotificationHistory();
        for (NotificationHistoryEntry entry : history) {
            tableModel.addRow(new Object[]{
                entry.getFormattedTimestamp(),
                entry.getRuleName(),
                entry.getUrl(),
                entry.getDetails()
            });
        }
    }
}

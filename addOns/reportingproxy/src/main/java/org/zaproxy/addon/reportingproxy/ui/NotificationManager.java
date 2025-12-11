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

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.view.View;

/**
 * Manages the display of notification windows for rule violations.
 */
public class NotificationManager {

    private static final Logger LOGGER = LogManager.getLogger(NotificationManager.class);
    private static final int MAX_VISIBLE_NOTIFICATIONS = 5;
    private static final int CORNER_MARGIN = 20;
    
    private static NotificationManager instance;
    private final List<NotificationWindow> activeNotifications;
    private final List<NotificationHistoryEntry> notificationHistory;
    
    /**
     * Private constructor for singleton pattern.
     */
    private NotificationManager() {
        activeNotifications = new ArrayList<>();
        notificationHistory = new ArrayList<>();
    }
    
    /**
     * Gets the singleton instance of the NotificationManager.
     * 
     * @return The singleton instance.
     */
    public static synchronized NotificationManager getInstance() {
        if (instance == null) {
            instance = new NotificationManager();
        }
        return instance;
    }
    
    /**
     * Shows a notification window for a rule violation.
     * 
     * @param ruleName The name of the rule that was triggered.
     * @param url The URL where the violation occurred.
     * @param details Additional details about the violation.
     */
    public void showNotification(String ruleName, String url, String details) {
        // Add to history
        notificationHistory.add(new NotificationHistoryEntry(ruleName, url, details));
        
        if (!View.isInitialised()) {
            LOGGER.debug("View not initialized, skipping pop-up notification");
            return;
        }
        
        javax.swing.SwingUtilities.invokeLater(() -> {
            try {
                java.awt.Window owner = View.getSingleton().getMainFrame();
                if (owner == null) {
                    LOGGER.warn("Main frame not available, cannot show notification");
                    return;
                }
                
                cleanupDisposedNotifications();
                
                if (activeNotifications.size() >= MAX_VISIBLE_NOTIFICATIONS) {
                    NotificationWindow oldest = activeNotifications.remove(0);
                    oldest.dispose();
                }
                
                NotificationWindow notification = new NotificationWindow(owner, ruleName, url, details);
                
                int positionY = CORNER_MARGIN;
                for (NotificationWindow existing : activeNotifications) {
                    if (existing.isVisible()) {
                        positionY += existing.getTotalHeight();
                    }
                }
                notification.setPositionY(positionY);
                
                activeNotifications.add(notification);
                
                notification.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosed(WindowEvent e) {
                        activeNotifications.remove(notification);
                        repositionNotifications();
                    }
                });
                
                notification.setVisible(true);
                
            } catch (Exception e) {
                LOGGER.error("Error showing notification: {}", e.getMessage(), e);
            }
        });
    }
    
    /**
     * Repositions all active notifications to ensure proper stacking.
     */
    private void repositionNotifications() {
        int positionY = CORNER_MARGIN;
        Iterator<NotificationWindow> iterator = activeNotifications.iterator();
        while (iterator.hasNext()) {
            NotificationWindow notification = iterator.next();
            if (!notification.isVisible() || notification.getOwner() == null) {
                iterator.remove();
                continue;
            }
            notification.setPositionY(positionY);
            positionY += notification.getTotalHeight();
        }
    }
    
    /**
     * Removes disposed notifications from the active list.
     */
    private void cleanupDisposedNotifications() {
        activeNotifications.removeIf(notification -> 
            !notification.isVisible() || notification.getOwner() == null
        );
    }
    
    /**
     * Closes all active notifications.
     */
    public void closeAllNotifications() {
        javax.swing.SwingUtilities.invokeLater(() -> {
            for (NotificationWindow notification : new ArrayList<>(activeNotifications)) {
                notification.dispose();
            }
            activeNotifications.clear();
        });
    }
    
    /**
     * Gets the number of currently active notifications.
     * 
     * @return The count of active notifications.
     */
    public int getActiveNotificationCount() {
        cleanupDisposedNotifications();
        return activeNotifications.size();
    }
    
    /**
     * Gets the notification history.
     * 
     * @return A copy of the notification history list.
     */
    public List<NotificationHistoryEntry> getNotificationHistory() {
        return new ArrayList<>(notificationHistory);
    }
    
    /**
     * Clears the notification history.
     */
    public void clearHistory() {
        notificationHistory.clear();
    }
    
    /**
     * Gets the number of notifications in history.
     * 
     * @return The count of notifications in history.
     */
    public int getHistoryCount() {
        return notificationHistory.size();
    }
}
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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.Timer;
import org.parosproxy.paros.view.View;

/**
 * Notification window that displays rule violation information.
 */
@SuppressWarnings("serial")
public class NotificationWindow extends JWindow {

    /** The logger for the notification window. */
    private static final Logger LOGGER = LogManager.getLogger(NotificationWindow.class);
    
    /** The default duration for the notification window in milliseconds. */
    private static final int DEFAULT_DURATION_MS = 5000;
    /** The duration for the fade animation in milliseconds. */
    private static final int FADE_DURATION_MS = 300;
    /** The number of steps for the fade animation. */
    private static final int FADE_STEPS = 20; // TODO: maybe change 
    /** The width of the notification window. */
    private static final int WINDOW_WIDTH = 350;
    /** The height of the notification window. */
    private static final int WINDOW_HEIGHT = 120;
    /** The margin for the notification window. */
    private static final int CORNER_MARGIN = 20;
    
    /** The label for the rule name. */
    private final JLabel ruleNameLabel;
    /** The label for the URL. */
    private final JLabel urlLabel;
    /** The label for the details. */
    private final JLabel detailsLabel;
    /** The timer for the close animation. */
    private final Timer closeTimer;
    /** The timer for the fade animation. */
    private final Timer fadeTimer;
    /** The current opacity of the notification window. */
    private float currentOpacity = 1.0f;
    /** The Y position of the notification window. */
    private int positionY;
    
    /**
     * Creates a new notification window with the specified rule violation information.
     * 
     * @param owner The owner window (typically the main ZAP frame).
     * @param ruleName The name of the rule that was triggered.
     * @param url The URL where the violation occurred.
     * @param details Additional details about the violation.
     */
    public NotificationWindow(java.awt.Window owner, String ruleName, String url, String details) {
        super(owner);
        
        setLayout(new BorderLayout());
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setAlwaysOnTop(true);
        setFocusableWindowState(false);
        
        // TODO: 
        JPanel contentPanel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                g2d.setColor(getBackground());
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                
                g2d.setColor(new Color(200, 200, 200));
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                
                g2d.dispose();
            }
        };
        contentPanel.setBackground(new Color(255, 255, 255));
        contentPanel.setOpaque(false);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 0, 2, 0);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Rule name label
        ruleNameLabel = new JLabel(ruleName);
        ruleNameLabel.setFont(ruleNameLabel.getFont().deriveFont(Font.BOLD, 13f));
        ruleNameLabel.setForeground(new Color(200, 50, 50)); // Red for violations
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        contentPanel.add(ruleNameLabel, gbc);
        
        // URL 
        String displayUrl = truncateString(url, 50);
        urlLabel = new JLabel(displayUrl);
        urlLabel.setFont(urlLabel.getFont().deriveFont(Font.PLAIN, 11f));
        urlLabel.setForeground(new Color(80, 80, 80));
        gbc.gridy = 1;
        contentPanel.add(urlLabel, gbc);
        
        // Details
        String displayDetails = truncateString(details, 60);
        detailsLabel = new JLabel(displayDetails);
        detailsLabel.setFont(detailsLabel.getFont().deriveFont(Font.PLAIN, 11f));
        detailsLabel.setForeground(new Color(100, 100, 100));
        gbc.gridy = 2;
        contentPanel.add(detailsLabel, gbc);
        
        add(contentPanel, BorderLayout.CENTER);
        
        // Position
        positionY = CORNER_MARGIN;
        updatePosition();
        
        closeTimer = new Timer(DEFAULT_DURATION_MS, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startFadeOut();
            }
        });
        closeTimer.setRepeats(false);
        
        // Fade out
        fadeTimer = new Timer(FADE_DURATION_MS / FADE_STEPS, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                currentOpacity -= (1.0f / FADE_STEPS);
                if (currentOpacity <= 0) {
                    fadeTimer.stop();
                    dispose();
                } else {
                    updateOpacity();
                }
            }
        });
        fadeTimer.setRepeats(true);
        
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                startFadeOut();
            }
        });
        
        setBackground(new Color(0, 0, 0, 0));
    }
    
    /**
     * Sets the Y position for stacking multiple notifications.
     * 
     * @param y The Y coordinate position.
     */
    public void setPositionY(int y) {
        this.positionY = y;
        updatePosition();
    }
    
    /**
     * Gets the current Y position.
     * 
     * @return The Y coordinate position.
     */
    public int getPositionY() {
        return positionY;
    }
    
    /**
     * Gets the height including margins for stacking calculations.
     * 
     * @return The total height including spacing.
     */
    public int getTotalHeight() {
        return getHeight() + 10; // Add spacing between notifications
    }
    
    /**
     * Updates the window position to the top-right corner.
     */
    private void updatePosition() {
        int x;
        if (View.isInitialised()) {
            java.awt.Window mainWindow = View.getSingleton().getMainFrame();
            if (mainWindow != null) {
                Point mainLocation = mainWindow.getLocation();
                Dimension mainSize = mainWindow.getSize();
                x = (int) (mainLocation.getX() + mainSize.getWidth() - WINDOW_WIDTH - CORNER_MARGIN);
            } else {
                // Fallback to screen size if main window not available
                java.awt.GraphicsEnvironment ge = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment();
                java.awt.GraphicsDevice gd = ge.getDefaultScreenDevice();
                java.awt.DisplayMode dm = gd.getDisplayMode();
                x = dm.getWidth() - WINDOW_WIDTH - CORNER_MARGIN;
            }
        } else {
            // View not initialized, use screen dimensions
            java.awt.GraphicsEnvironment ge = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment();
            java.awt.GraphicsDevice gd = ge.getDefaultScreenDevice();
            java.awt.DisplayMode dm = gd.getDisplayMode();
            x = dm.getWidth() - WINDOW_WIDTH - CORNER_MARGIN;
        }
        setLocation(x, positionY);
    }
    
    /**
     * Shows the notification window with fade-in animation.
     */
    @Override
    public void setVisible(boolean visible) {
        if (visible) {
            currentOpacity = 0.0f;
            updateOpacity();
            super.setVisible(true);
            startFadeIn();
            closeTimer.start();
        } else {
            super.setVisible(false);
        }
    }
    
    /**
     * Starts the fade-in animation.
     */
    private void startFadeIn() {
        // Fade in
        Timer fadeInTimer = new Timer(FADE_DURATION_MS / FADE_STEPS, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                currentOpacity += (1.0f / FADE_STEPS);
                if (currentOpacity >= 1.0f) {
                    currentOpacity = 1.0f;
                    ((Timer) e.getSource()).stop();
                }
                updateOpacity();
            }
        });
        fadeInTimer.setRepeats(true);
        fadeInTimer.start();
    }
    
    /**
     * Starts the fade-out animation and closes the window.
     */
    private void startFadeOut() {
        closeTimer.stop();
        if (fadeTimer.isRunning()) {
            return;
        }
        fadeTimer.start();
    }
    
    /**
     * Updates the window opacity.
     */
    private void updateOpacity() {
        try {
            setOpacity(currentOpacity);
        } catch (UnsupportedOperationException e) {
            // Some systems don't support opacity, just ignore it
            LOGGER.debug("Opacity not supported on this system");
        }
    }
    
    /**
     * Truncates a string if it exceeds the specified length.
     * 
     * @param str The string to truncate.
     * @param maxLength The maximum length.
     * @return The truncated string with "..." if needed.
     */
    private String truncateString(String str, int maxLength) {
        if (str == null) {
            return "";
        }
        if (str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength - 3) + "...";
    }
    
    /**
     * Paints the window with shadow effect.
     */
    @Override
    public void paint(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Draw shadow
        g2d.setColor(new Color(0, 0, 0, 30));
        g2d.fillRoundRect(3, 3, getWidth() - 6, getHeight() - 6, 8, 8);
        
        g2d.dispose();
        super.paint(g);
    }
    
    /**
     * Cleans up resources when the window is disposed.
     */
    @Override
    public void dispose() {
        if (closeTimer != null) {
            closeTimer.stop();
        }
        if (fadeTimer != null) {
            fadeTimer.stop();
        }
        super.dispose();
    }
}
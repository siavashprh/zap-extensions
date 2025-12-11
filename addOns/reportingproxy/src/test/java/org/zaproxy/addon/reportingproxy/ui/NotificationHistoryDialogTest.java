package org.zaproxy.addon.reportingproxy.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.awt.Frame;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class NotificationHistoryDialogTest {

    private NotificationManager mockManager;
    private Frame mockOwner;
    private NotificationHistoryDialog dialog;

    @BeforeEach
    void setUp() {
        mockManager = mock(NotificationManager.class);
        mockOwner = mock(Frame.class);
    }

    @Test
    void shouldInitializeTableWithHistory() throws Exception {
        NotificationHistoryEntry entry = new NotificationHistoryEntry("Rule1", "http://example.com", "Details");
        when(mockManager.getNotificationHistory()).thenReturn(List.of(entry));

        SwingUtilities.invokeAndWait(() -> {
            // Pass null as owner to avoid issues with mocking Frame
            dialog = new NotificationHistoryDialog(null, mockManager);
        });

        JTable table = findTable(dialog);
        assertEquals(1, table.getRowCount());
        assertEquals("Rule1", table.getValueAt(0, 1));
        assertEquals("http://example.com", table.getValueAt(0, 2));
        assertEquals("Details", table.getValueAt(0, 3));
    }

    @Test
    void shouldClearHistory() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            dialog = new NotificationHistoryDialog(null, mockManager);
        });

        JButton clearButton = findButton(dialog, "Clear History");
        
        SwingUtilities.invokeAndWait(() -> {
            clearButton.doClick();
        });

        verify(mockManager).clearHistory();
    }

    @Test
    void shouldRefreshTable() throws Exception {
        when(mockManager.getNotificationHistory()).thenReturn(List.of());
        
        SwingUtilities.invokeAndWait(() -> {
            dialog = new NotificationHistoryDialog(null, mockManager);
        });
        
        // Now add an entry and refresh
        NotificationHistoryEntry entry = new NotificationHistoryEntry("Rule1", "http://example.com", "Details");
        when(mockManager.getNotificationHistory()).thenReturn(List.of(entry));
        
        JButton refreshButton = findButton(dialog, "Refresh");
        
        SwingUtilities.invokeAndWait(() -> {
            refreshButton.doClick();
        });
        
        JTable table = findTable(dialog);
        assertEquals(1, table.getRowCount());
    }

    private JTable findTable(NotificationHistoryDialog dialog) {
        for (java.awt.Component comp : dialog.getContentPane().getComponents()) {
            if (comp instanceof JScrollPane) {
                JScrollPane scroll = (JScrollPane) comp;
                if (scroll.getViewport().getView() instanceof JTable) {
                    return (JTable) scroll.getViewport().getView();
                }
            }
        }
        throw new RuntimeException("JTable not found in dialog");
    }

    private JButton findButton(NotificationHistoryDialog dialog, String text) {
        // Buttons are in a JPanel at the SOUTH
        for (java.awt.Component comp : dialog.getContentPane().getComponents()) {
            if (comp instanceof javax.swing.JPanel) {
                javax.swing.JPanel panel = (javax.swing.JPanel) comp;
                for (java.awt.Component innerComp : panel.getComponents()) {
                    if (innerComp instanceof JButton) {
                        JButton btn = (JButton) innerComp;
                        if (btn.getText().equals(text)) {
                            return btn;
                        }
                    }
                }
            }
        }
        throw new RuntimeException("Button '" + text + "' not found");
    }
}

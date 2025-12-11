package org.zaproxy.addon.reportingproxy.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.zaproxy.addon.reportingproxy.ExtensionReportingProxy;
import org.zaproxy.addon.reportingproxy.ReportingProxyController;
import org.zaproxy.addon.reportingproxy.ReportingRule;

class ReportingProxyPanelTest {

    private ExtensionReportingProxy mockExtension;
    private ReportingProxyController mockController;
    private ReportingRule mockRule;
    private ReportingProxyPanel panel;

    @BeforeEach
    void setUp() throws Exception {
        mockExtension = mock(ExtensionReportingProxy.class);
        mockController = mock(ReportingProxyController.class);
        mockRule = mock(ReportingRule.class);

        when(mockExtension.getController()).thenReturn(mockController);
        when(mockController.getRules()).thenReturn(List.of(mockRule));
        
        when(mockRule.getName()).thenReturn("TestRule");
        when(mockRule.getDescription()).thenReturn("Test Description");
        when(mockRule.isBlocking()).thenReturn(false);
        when(mockRule.getBlockedCount()).thenReturn(5);
    }

    @Test
    void shouldInitializeTableWithRules() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            panel = new ReportingProxyPanel(mockExtension);
        });
        
        SwingUtilities.invokeAndWait(() -> {});

        JTable table = findTable(panel);
        assertEquals(1, table.getRowCount());
        assertEquals("TestRule", table.getValueAt(0, 0));
        assertEquals("Test Description", table.getValueAt(0, 1));
        assertEquals(false, table.getValueAt(0, 2));
        assertEquals(5, table.getValueAt(0, 3));
    }

    @Test
    void shouldUpdateRuleWhenBlockingToggled() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            panel = new ReportingProxyPanel(mockExtension);
        });
        SwingUtilities.invokeAndWait(() -> {});

        JTable table = findTable(panel);
        
        SwingUtilities.invokeAndWait(() -> {
            table.setValueAt(true, 0, 2);
        });

        verify(mockRule).setBlocking(true);
    }

    private JTable findTable(ReportingProxyPanel panel) {
        for (java.awt.Component comp : panel.getComponents()) {
            if (comp instanceof JScrollPane) {
                JScrollPane scroll = (JScrollPane) comp;
                if (scroll.getViewport().getView() instanceof JTable) {
                    return (JTable) scroll.getViewport().getView();
                }
            }
        }
        throw new RuntimeException("JTable not found in panel");
    }
}

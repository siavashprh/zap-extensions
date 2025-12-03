package org.zaproxy.addon.reportingproxy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.parosproxy.paros.network.HttpMessage;

class ReportingProxyControllerTest {

    private ReportingProxyController controller;
    private ReportingRule mockRule1;
    private ReportingRule mockRule2;

    @BeforeEach
    void setUp() {
        controller = new ReportingProxyController();
        mockRule1 = mock(ReportingRule.class);
        mockRule2 = mock(ReportingRule.class);
    }

    @Test
    void shouldAddRule() {
        controller.addRule(mockRule1);
        assertEquals(1, controller.getRules().size());
        assertTrue(controller.getRules().contains(mockRule1));
    }

    @Test
    void shouldClearRules() {
        controller.addRule(mockRule1);
        controller.clearRules();
        assertTrue(controller.getRules().isEmpty());
    }

    @Test
    void shouldScanWithAllRules() {
        controller.addRule(mockRule1);
        controller.addRule(mockRule2);
        HttpMessage msg = new HttpMessage();

        controller.scan(msg);

        verify(mockRule1, times(1)).scan(msg);
        verify(mockRule2, times(1)).scan(msg);
    }

    @Test
    void shouldContinueScanningWhenOneRuleFails() {
        controller.addRule(mockRule1);
        controller.addRule(mockRule2);
        HttpMessage msg = new HttpMessage();

        // Make rule 1 throw an exception
        doThrow(new RuntimeException("Rule failed")).when(mockRule1).scan(msg);

        controller.scan(msg);

        // Verify rule 2 still ran
        verify(mockRule2, times(1)).scan(msg);
    }
}

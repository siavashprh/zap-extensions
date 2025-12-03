package org.zaproxy.addon.reportingproxy;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.parosproxy.paros.network.HttpMessage;

class ExtensionReportingProxyTest {

    private ExtensionReportingProxy extension;
    private ReportingRule mockRule;

    @BeforeEach
    void setUp() {
        extension = new ExtensionReportingProxy();
        org.parosproxy.paros.extension.ExtensionHook mockHook = mock(org.parosproxy.paros.extension.ExtensionHook.class);
        
        // Initialize extension hook to setup controller and listener
        extension.hook(mockHook);
        
        mockRule = mock(ReportingRule.class);
    }

    @Test
    void shouldAddRule() {
        extension.getController().addRule(mockRule);
        extension.getController().scan(new HttpMessage());
        verify(mockRule, times(1)).scan(org.mockito.ArgumentMatchers.any(HttpMessage.class));
    }

    @Test
    void shouldClearRules() {
        extension.getController().addRule(mockRule);
        extension.getController().clearRules();
        extension.getController().scan(new HttpMessage());
        verify(mockRule, times(0)).scan(org.mockito.ArgumentMatchers.any(HttpMessage.class));
    }

    @Test
    void shouldBeUnloadable() {
        assertTrue(extension.canUnload());
    }

    @Test
    void shouldReturnController() {
        org.junit.jupiter.api.Assertions.assertNotNull(extension.getController());
    }
}

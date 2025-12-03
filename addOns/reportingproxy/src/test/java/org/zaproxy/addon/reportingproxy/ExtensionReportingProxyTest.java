package org.zaproxy.addon.reportingproxy;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

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
        org.parosproxy.paros.extension.ExtensionHookView mockView = mock(org.parosproxy.paros.extension.ExtensionHookView.class);
        
        // Handle the getView check in hook()
        // We can't easily mock getView() on the extension itself without a partial mock, 
        // but we can ensure getHookView returns a mock to avoid NPE if it is called.
        // However, extension.getView() comes from ExtensionAdaptor and is usually null until init.
        // The code checks: if (getView() != null). 
        // In a unit test, getView() is likely null, so the UI part is skipped.
        // But we still need hook() to run to init the controller.
        
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
}

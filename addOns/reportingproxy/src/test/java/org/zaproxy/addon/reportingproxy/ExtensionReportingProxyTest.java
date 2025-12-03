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
        mockRule = mock(ReportingRule.class);
    }

    @Test
    void shouldAddRule() {
        extension.addRule(mockRule);
        extension.onHttpRequestSend(new HttpMessage(), 0, null);
        verify(mockRule, times(1)).scan(org.mockito.ArgumentMatchers.any(HttpMessage.class));
    }

    @Test
    void shouldClearRules() {
        extension.addRule(mockRule);
        extension.clearRules();
        extension.onHttpRequestSend(new HttpMessage(), 0, null);
        verify(mockRule, times(0)).scan(org.mockito.ArgumentMatchers.any(HttpMessage.class));
    }
}

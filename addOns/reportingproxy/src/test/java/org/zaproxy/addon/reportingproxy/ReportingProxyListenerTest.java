package org.zaproxy.addon.reportingproxy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.parosproxy.paros.network.HttpMessage;

class ReportingProxyListenerTest {

    private ReportingProxyListener listener;
    private ReportingProxyController mockController;

    @BeforeEach
    void setUp() {
        mockController = mock(ReportingProxyController.class);
        listener = new ReportingProxyListener(mockController);
    }

    @Test
    void shouldHaveCorrectListenerOrder() {
        assertEquals(9000, listener.getListenerOrder());
    }

    @Test
    void shouldDelegateOnHttpRequestSend() {
        HttpMessage msg = new HttpMessage();
        listener.onHttpRequestSend(msg, 0, null);
        verify(mockController, times(1)).scan(msg);
    }

    @Test
    void shouldDelegateOnHttpResponseReceive() {
        HttpMessage msg = new HttpMessage();
        listener.onHttpResponseReceive(msg, 0, null);
        verify(mockController, times(1)).scan(msg);
    }
}

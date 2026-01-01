package org.zaproxy.addon.reportingproxy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
        assertEquals(9000, listener.getArrangeableListenerOrder());
    }

    @Test
    void shouldDelegateOnHttpRequestSend() {
        HttpMessage msg = new HttpMessage();
        when(mockController.scan(msg)).thenReturn(true);
        
        boolean result = listener.onHttpRequestSend(msg);
        
        verify(mockController, times(1)).scan(msg);
        assertTrue(result);
    }

    @Test
    void shouldReturnTrueWhenBlockedOnRequest() {
        HttpMessage msg = new HttpMessage();
        when(mockController.scan(msg)).thenReturn(false);
        
        boolean result = listener.onHttpRequestSend(msg);
        
        verify(mockController, times(1)).scan(msg);
        assertTrue(result);
    }

    @Test
    void shouldDelegateOnHttpResponseReceive() {
        HttpMessage msg = new HttpMessage();
        when(mockController.scan(msg)).thenReturn(true);
        
        boolean result = listener.onHttpResponseReceive(msg);
        
        verify(mockController, times(1)).scan(msg);
        assertTrue(result);
    }

    @Test
    void shouldReturnTrueWhenBlockedOnResponse() {
        HttpMessage msg = new HttpMessage();
        when(mockController.scan(msg)).thenReturn(false);
        
        boolean result = listener.onHttpResponseReceive(msg);
        
        verify(mockController, times(1)).scan(msg);
        assertTrue(result);
    }
}

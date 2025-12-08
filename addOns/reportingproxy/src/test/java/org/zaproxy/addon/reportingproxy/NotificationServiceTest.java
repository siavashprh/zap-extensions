package org.zaproxy.addon.reportingproxy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.parosproxy.paros.network.HttpMessage;

class NotificationServiceTest {

    private NotificationService service;
    private NotificationManager manager;

    @BeforeEach
    void setUp() {
        service = NotificationService.getSingleton();
        manager = NotificationManager.getInstance();
        manager.clearHistory();
    }

    @Test
    void shouldReturnSameInstance() {
        NotificationService instance1 = NotificationService.getSingleton();
        NotificationService instance2 = NotificationService.getSingleton();
        assertSame(instance1, instance2);
    }

    @Test
    void shouldExtractUrlFromMessage() throws Exception {
        ReportingRule mockRule = mock(ReportingRule.class);
        when(mockRule.getName()).thenReturn("TestRule");
        
        HttpMessage msg = new HttpMessage();
        msg.setRequestHeader("GET http://example.com/path HTTP/1.1\r\nHost: example.com\r\n\r\n");

        service.notify(mockRule, msg, "Details");

        var history = manager.getNotificationHistory();
        assertEquals(1, history.size());
        assertEquals("http://example.com/path", history.get(0).getUrl());
    }

    @Test
    void shouldHandleNullMessage() {
        ReportingRule mockRule = mock(ReportingRule.class);
        when(mockRule.getName()).thenReturn("TestRule");

        service.notify(mockRule, null, "Details");

        var history = manager.getNotificationHistory();
        assertEquals(1, history.size());
        assertEquals("Unknown URL", history.get(0).getUrl());
    }

    @Test
    void shouldNotifyViaLoggerWhenViewNotAvailable() {
        ReportingRule mockRule = mock(ReportingRule.class);
        when(mockRule.getName()).thenReturn("TestRule");
        HttpMessage msg = new HttpMessage();

        service.notify(mockRule, msg, "Details");
    }
}

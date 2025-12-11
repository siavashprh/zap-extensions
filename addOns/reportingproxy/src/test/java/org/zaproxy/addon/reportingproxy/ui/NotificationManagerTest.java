package org.zaproxy.addon.reportingproxy.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class NotificationManagerTest {

    private NotificationManager manager;

    @BeforeEach
    void setUp() {
        manager = NotificationManager.getInstance();
        manager.clearHistory();
    }

    @Test
    void shouldReturnSameInstance() {
        NotificationManager instance1 = NotificationManager.getInstance();
        NotificationManager instance2 = NotificationManager.getInstance();
        assertSame(instance1, instance2);
    }

    @Test
    void shouldAddToHistory() {
        manager.showNotification("TestRule", "http://test.com", "Test details");
        assertEquals(1, manager.getHistoryCount());
    }

    @Test
    void shouldClearHistory() {
        manager.showNotification("Rule1", "http://test1.com", "Details1");
        manager.showNotification("Rule2", "http://test2.com", "Details2");
        
        assertEquals(2, manager.getHistoryCount());
        
        manager.clearHistory();
        
        assertEquals(0, manager.getHistoryCount());
        assertTrue(manager.getNotificationHistory().isEmpty());
    }

    @Test
    void shouldGetHistoryCount() {
        assertEquals(0, manager.getHistoryCount());
        
        manager.showNotification("Rule1", "http://test1.com", "Details1");
        assertEquals(1, manager.getHistoryCount());
        
        manager.showNotification("Rule2", "http://test2.com", "Details2");
        assertEquals(2, manager.getHistoryCount());
    }

    @Test
    void shouldReturnHistoryEntries() {
        manager.showNotification("TestRule", "http://test.com", "Test details");
        
        var history = manager.getNotificationHistory();
        assertEquals(1, history.size());
        assertEquals("TestRule", history.get(0).getRuleName());
        assertEquals("http://test.com", history.get(0).getUrl());
    }
}


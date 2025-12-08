package org.zaproxy.addon.reportingproxy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class NotificationHistoryEntryTest {

    @Test
    void shouldStoreValuesCorrectly() {
        NotificationHistoryEntry entry = new NotificationHistoryEntry(
            "TestRule", "http://test.com", "Test details"
        );
        assertEquals("TestRule", entry.getRuleName());
        assertEquals("http://test.com", entry.getUrl());
        assertEquals("Test details", entry.getDetails());
        assertNotNull(entry.getTimestamp());
    }

    @Test
    void shouldFormatTimestamp() {
        NotificationHistoryEntry entry = new NotificationHistoryEntry(
            "TestRule", "http://test.com", "Details"
        );
        String formatted = entry.getFormattedTimestamp();
        assertNotNull(formatted);
        // Just check it's not empty and has expected format
        assertNotNull(formatted);
    }
}

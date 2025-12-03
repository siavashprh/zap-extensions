package org.zaproxy.addon.reportingproxy.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpRequestHeader;

class RateLimitRuleTest {

    private TestableRateLimitRule rule;
    private List<String> notifications;

    @BeforeEach
    void setUp() {
        rule = new TestableRateLimitRule();
        notifications = new ArrayList<>();
    }

    @Test
    void shouldNotNotifyUnderThreshold() throws Exception {
        // Given
        HttpMessage msg = createMessage("example.com");

        // When
        for (int i = 0; i < 10; i++) {
            rule.scan(msg);
        }

        // Then
        assertTrue(rule.notifications.isEmpty(), "Should not notify when under or at threshold");
    }

    @Test
    void shouldNotifyWhenThresholdExceeded() throws Exception {
        // Given
        HttpMessage msg = createMessage("example.com");

        // When
        for (int i = 0; i < 11; i++) {
            rule.scan(msg);
        }

        // Then
        assertEquals(1, rule.notifications.size(), "Should notify once when threshold exceeded");
        assertTrue(
                rule.notifications.get(0).contains("Rate limit exceeded"),
                "Notification should contain correct message");
    }

    @Test
    void shouldRespectTimeWindow() throws Exception {
        HttpMessage msg = createMessage("example.com");
        long startTime = 1000000; // Arbitrary start time
        rule.setCurrentTime(startTime);

        for (int i = 0; i < 10; i++) {
            rule.scan(msg);
        }
        
        assertTrue(rule.notifications.isEmpty(), "Should be at threshold, no alert yet");

        rule.setCurrentTime(startTime + 11000);
        
        rule.scan(msg);

        assertTrue(rule.notifications.isEmpty(), "Should not notify because old requests expired");
        
        for (int i = 0; i < 10; i++) {
            rule.scan(msg);
        }
        
        assertEquals(1, rule.notifications.size(), "Should notify now that threshold is exceeded in new window");
    }

    private HttpMessage createMessage(String host) throws Exception {
        HttpMessage msg = new HttpMessage();
        msg.setRequestHeader("GET / HTTP/1.1\r\nHost: " + host + "\r\n\r\n");
        return msg;
    }

    // Subclass to capture notifications and mock time
    private static class TestableRateLimitRule extends RateLimitRule {
        List<String> notifications = new ArrayList<>();
        long currentTime = System.currentTimeMillis();

        void setCurrentTime(long time) {
            this.currentTime = time;
        }

        @Override
        protected long getCurrentTime() {
            return currentTime;
        }

        @Override
        protected void notifyViolation(HttpMessage msg, String details) {
            notifications.add(details);
        }
    }
}

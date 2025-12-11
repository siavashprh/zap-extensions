package org.zaproxy.addon.reportingproxy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.parosproxy.paros.network.HttpMessage;

class AbstractReportingRuleTest {

    private TestReportingRule rule;

    @BeforeEach
    void setUp() {
        rule = new TestReportingRule();
    }

    @Test
    void shouldDefaultToNonBlocking() {
        assertFalse(rule.isBlocking());
    }

    @Test
    void shouldSetBlocking() {
        rule.setBlocking(true);
        assertTrue(rule.isBlocking());
        
        rule.setBlocking(false);
        assertFalse(rule.isBlocking());
    }

    @Test
    void shouldIncrementBlockedCount() {
        assertEquals(0, rule.getBlockedCount());
        
        rule.incrementBlockedCount();
        assertEquals(1, rule.getBlockedCount());
        
        rule.incrementBlockedCount();
        assertEquals(2, rule.getBlockedCount());
    }

    private static class TestReportingRule extends AbstractReportingRule {
        @Override
        public void scan(HttpMessage msg) {
            // No-op
        }

        @Override
        public String getName() {
            return "TestRule";
        }

        @Override
        public String getDescription() {
            return "Test Description";
        }
    }
}

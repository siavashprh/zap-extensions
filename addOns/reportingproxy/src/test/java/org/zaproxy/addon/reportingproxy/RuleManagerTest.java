package org.zaproxy.addon.reportingproxy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RuleManagerTest {

    private RuleManager ruleManager;
    private ReportingRule mockRule1;
    private ReportingRule mockRule2;

    @BeforeEach
    void setUp() {
        ruleManager = new RuleManager();
        mockRule1 = mock(ReportingRule.class);
        mockRule2 = mock(ReportingRule.class);
        
        when(mockRule1.getName()).thenReturn("Rule1");
        when(mockRule2.getName()).thenReturn("Rule2");
    }

    @Test
    void shouldAddRule() {
        ruleManager.addRule(mockRule1);
        assertEquals(1, ruleManager.getRules().size());
        assertTrue(ruleManager.getRules().contains(mockRule1));
    }

    @Test
    void shouldNotAddDuplicateRule() {
        when(mockRule2.getName()).thenReturn("Rule1"); // Same name as Rule1

        ruleManager.addRule(mockRule1);
        ruleManager.addRule(mockRule2);

        assertEquals(1, ruleManager.getRules().size());
        assertTrue(ruleManager.getRules().contains(mockRule1));
    }

    @Test
    void shouldRemoveRule() {
        ruleManager.addRule(mockRule1);
        ruleManager.removeRule(mockRule1);
        assertTrue(ruleManager.getRules().isEmpty());
    }

    @Test
    void shouldClearRules() {
        ruleManager.addRule(mockRule1);
        ruleManager.addRule(mockRule2);
        ruleManager.clearRules();
        assertTrue(ruleManager.getRules().isEmpty());
    }

    @Test
    void shouldReturnUnmodifiableList() {
        ruleManager.addRule(mockRule1);
        assertFalse(ruleManager.getRules().isEmpty());
        
        try {
            ruleManager.getRules().add(mockRule2);
            assertTrue(false, "Should have thrown UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // Expected
        }
    }
}

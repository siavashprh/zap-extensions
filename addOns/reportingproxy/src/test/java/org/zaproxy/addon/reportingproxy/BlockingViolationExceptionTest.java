package org.zaproxy.addon.reportingproxy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;

class BlockingViolationExceptionTest {

    @Test
    void shouldStoreRuleAndDetails() {
        ReportingRule mockRule = mock(ReportingRule.class);
        String details = "Violation details";

        BlockingViolationException exception = new BlockingViolationException(mockRule, details);

        assertSame(mockRule, exception.getRule());
        assertEquals(details, exception.getDetails());
        assertEquals(details, exception.getMessage());
    }
}

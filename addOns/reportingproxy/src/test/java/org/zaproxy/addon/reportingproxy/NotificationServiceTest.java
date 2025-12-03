package org.zaproxy.addon.reportingproxy;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.parosproxy.paros.network.HttpMessage;

class NotificationServiceTest {

    @Test
    void shouldNotifyViaLoggerWhenViewNotAvailable() {
        ReportingRule mockRule = mock(ReportingRule.class);
        when(mockRule.getName()).thenReturn("TestRule");
        HttpMessage msg = new HttpMessage();

        NotificationService.getSingleton().notify(mockRule, msg, "Details");
    }
}

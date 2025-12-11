# Reporting Proxy & Filtering Proxy ZAP Extension

This extension transforms ZAP into a passive monitoring tool (Reporting Proxy) and an active defender (Filtering Proxy). It allows developers to load custom rules to observe traffic and optionally block requests that violate those rules.

## Features

*   **Live Notifications**: Get immediate alerts when a rule is violated.
*   **Active Blocking**: Configure rules to block traffic and return custom error pages.
*   **Custom Rules**: Load your own logic via external JAR files.
*   **Statistics**: Track how many times each rule has been triggered or blocked.
*   **Graceful Handling**:
    *   **API (JSON)**: Returns `429 Too Many Requests` with a JSON error message.
    *   **Web (HTML)**: Returns a user-friendly "Request Blocked" HTML page.

## Usage

### Installation
1.  Build the add-on using Gradle:
    ```bash
    ./gradlew :addOns:reportingproxy:assemble
    ```
2.  The add-on file (`reportingproxy-release.zap`) will be generated in `zap-extensions/addOns/reportingproxy/build/libs/`.
3.  In ZAP, go to **File** -> **Load Add-on File...** and select the generated `.zap` file.

### Using the Panel
Once installed, a new tab titled **"Reporting Proxy"** will appear in the bottom panel of ZAP. The table displays all currently active rules.

*   **Load Rules**: Click the **"Load Rules JAR"** button to select a `.jar` file containing your custom rules.
*   **Toggle Blocking**: Check the box in the **"Blocking"** column to enable active blocking for a specific rule.
*   **View Statistics**: The **"Blocked Count"** column shows how many requests have been blocked by each rule.
*   **Remove Rules**: Select a rule in the table and click **"Remove Rule"** to unload it.
*   **History**: Click **"View Notification History"** to see a log of all past alerts.

## Writing Custom Rules

You can create your own rules to detect specific patterns or vulnerabilities.

### Prerequisites
*   Java Development Kit (JDK) 11 or higher.
*   `zap.jar` (or the ZAP core dependencies) on your classpath.
*   `reportingproxy.jar` (this extension) on your classpath.

### Step-by-Step Guide

1.  **Create a new Java project**.
2.  **Implement the `ReportingRule` interface**. Your class must implement `org.zaproxy.addon.reportingproxy.ReportingRule`.
    *   *Tip*: You can extend `org.zaproxy.addon.reportingproxy.AbstractReportingRule` to get free support for blocking state and statistics.

### Example Rule

Here is an example of a rule that detects rate limiting violations:

```java
package my.custom.rules;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.addon.reportingproxy.AbstractReportingRule; // Use Abstract base class
import org.zaproxy.addon.reportingproxy.NotificationService;

public class RateLimitRule extends AbstractReportingRule {

    private static final int THRESHOLD = 10;
    private static final long TIME_WINDOW = 10000; // 10 seconds

    private Map<String, Queue<Long>> requestHistory = new HashMap<>();

    @Override
    public void scan(HttpMessage msg) {
        // Only care about requests
        if (msg.getResponseHeader().isEmpty()) {
            String domain = msg.getRequestHeader().getHostName();
            if (domain == null) {
                return;
            }
            long now = System.currentTimeMillis();

            requestHistory.putIfAbsent(domain, new LinkedList<>());
            Queue<Long> timestamps = requestHistory.get(domain);

            // Add current timestamp
            timestamps.add(now);

            // Remove old timestamps
            while (!timestamps.isEmpty() && now - timestamps.peek() > TIME_WINDOW) {
                timestamps.poll();
            }

            // Check threshold
            if (timestamps.size() > THRESHOLD) {
                // This call handles both notification AND blocking (if enabled)
                NotificationService.getSingleton().notify(
                        this,
                        msg,
                        "Rate limit exceeded for " + domain);
            }
        }
    }

    @Override
    public String getName() {
        return "Rate Limit Rule";
    }

    @Override
    public String getDescription() {
        return "Detects if request count to a domain exceeds threshold in a time window.";
    }
}
```

### Packaging
1.  Compile your rule class.
2.  Package it into a JAR file.
    ```bash
    jar cvf my-custom-rules.jar my/custom/rules/RateLimitRule.class
    ```
3.  Load this JAR using the **"Load Rules JAR"** button in the Reporting Proxy panel.

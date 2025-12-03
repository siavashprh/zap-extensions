description = "ReportingProxy extension"

zapAddOn {
    addOnName.set("ReportingProxy")

    manifest {
        author.set("Group 8")
    }
}

crowdin {
    configuration {
        val resourcesPath = "org/zaproxy/addon/${zapAddOn.addOnId.get()}/resources/"
        tokens.put("%messagesPath%", resourcesPath)
        tokens.put("%helpPath%", resourcesPath)
    }
}

dependencies {
    testImplementation(project(":testutils"))
    testImplementation(libs.test.junit.jupiter)
    testImplementation(libs.test.mockito.junit.jupiter)
    testImplementation("org.mockito:mockito-inline:5.2.0")
}

tasks.test {
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

tasks.register<Jar>("packageHeaderAnalysisRule") {
    archiveClassifier.set("header-analysis-rule")
    from(sourceSets.main.get().output) {
        include("org/zaproxy/addon/reportingproxy/ReportingRule.class")
        include("org/zaproxy/addon/reportingproxy/NotificationService.class")
        include("org/zaproxy/addon/reportingproxy/NotificationService$*.class")
        include("org/zaproxy/addon/reportingproxy/rules/HeaderAnalysisRule.class")
        include("org/zaproxy/addon/reportingproxy/rules/HeaderAnalysisRule$*.class")
    }
}

tasks.register<Jar>("packageRateLimitRule") {
    archiveClassifier.set("rate-limit-rule")
    from(sourceSets.main.get().output) {
        include("org/zaproxy/addon/reportingproxy/ReportingRule.class")
        include("org/zaproxy/addon/reportingproxy/NotificationService.class")
        include("org/zaproxy/addon/reportingproxy/NotificationService$*.class")
        include("org/zaproxy/addon/reportingproxy/rules/RateLimitRule.class")
        include("org/zaproxy/addon/reportingproxy/rules/RateLimitRule$*.class")
    }
}

tasks.register<Jar>("packageCookieSyncRule") {
    archiveClassifier.set("cookie-sync-rule")
    from(sourceSets.main.get().output) {
        include("org/zaproxy/addon/reportingproxy/ReportingRule.class")
        include("org/zaproxy/addon/reportingproxy/NotificationService.class")
        include("org/zaproxy/addon/reportingproxy/NotificationService$*.class")
        include("org/zaproxy/addon/reportingproxy/rules/CookieSyncRule.class")
        include("org/zaproxy/addon/reportingproxy/rules/CookieSyncRule$*.class")
    }
}

tasks.register<Jar>("packageCspDetectionRule") {
    archiveClassifier.set("csp-detection-rule")
    from(sourceSets.main.get().output) {
        include("org/zaproxy/addon/reportingproxy/ReportingRule.class")
        include("org/zaproxy/addon/reportingproxy/NotificationService.class")
        include("org/zaproxy/addon/reportingproxy/NotificationService$*.class")
        include("org/zaproxy/addon/reportingproxy/rules/CspDetectionRule.class")
        include("org/zaproxy/addon/reportingproxy/rules/CspDetectionRule$*.class")
    }
}

tasks.register("packageAllRules") {
    dependsOn("packageHeaderAnalysisRule", "packageRateLimitRule", "packageCookieSyncRule", "packageCspDetectionRule")
    group = "build"
    description = "Packages all reporting rules as individual JAR files"
}

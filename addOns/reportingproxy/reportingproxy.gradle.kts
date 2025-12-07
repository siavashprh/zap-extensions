import org.zaproxy.gradle.addon.AddOnPlugin

description = "ReportingProxy extension"

val rulesJarDir = layout.buildDirectory.dir("zapAddOn/rules/")

zapAddOn {
    addOnName.set("ReportingProxy")

    manifest {
        author.set("Group 8")
        files.from(rulesJarDir)
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
    archiveBaseName.set("header-analysis-rule")
    archiveClassifier.set("")
    destinationDirectory.set(rulesJarDir.get().asFile)
    dependsOn(tasks.named("compileJava"))
    from(sourceSets.main.get().output) {
        include("org/zaproxy/addon/reportingproxy/ReportingRule.class")
        include("org/zaproxy/addon/reportingproxy/NotificationService.class")
        include("org/zaproxy/addon/reportingproxy/NotificationService$*.class")
        include("org/zaproxy/addon/reportingproxy/rules/HeaderAnalysisRule.class")
        include("org/zaproxy/addon/reportingproxy/rules/HeaderAnalysisRule$*.class")
    }
}

tasks.register<Jar>("packageRateLimitRule") {
    archiveBaseName.set("rate-limit-rule")
    archiveClassifier.set("")
    destinationDirectory.set(rulesJarDir.get().asFile)
    dependsOn(tasks.named("compileJava"))
    from(sourceSets.main.get().output) {
        include("org/zaproxy/addon/reportingproxy/ReportingRule.class")
        include("org/zaproxy/addon/reportingproxy/NotificationService.class")
        include("org/zaproxy/addon/reportingproxy/NotificationService$*.class")
        include("org/zaproxy/addon/reportingproxy/rules/RateLimitRule.class")
        include("org/zaproxy/addon/reportingproxy/rules/RateLimitRule$*.class")
    }
}

tasks.register<Jar>("packageCookieSyncRule") {
    archiveBaseName.set("cookie-sync-rule")
    archiveClassifier.set("")
    destinationDirectory.set(rulesJarDir.get().asFile)
    dependsOn(tasks.named("compileJava"))
    from(sourceSets.main.get().output) {
        include("org/zaproxy/addon/reportingproxy/ReportingRule.class")
        include("org/zaproxy/addon/reportingproxy/NotificationService.class")
        include("org/zaproxy/addon/reportingproxy/NotificationService$*.class")
        include("org/zaproxy/addon/reportingproxy/rules/CookieSyncRule.class")
        include("org/zaproxy/addon/reportingproxy/rules/CookieSyncRule$*.class")
    }
}

tasks.register<Jar>("packageCspDetectionRule") {
    archiveBaseName.set("csp-detection-rule")
    archiveClassifier.set("")
    destinationDirectory.set(rulesJarDir.get().asFile)
    dependsOn(tasks.named("compileJava"))
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

tasks.named(AddOnPlugin.GENERATE_MANIFEST_TASK_NAME) {
    dependsOn("packageAllRules")
}
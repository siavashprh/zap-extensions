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
}

tasks.register<Jar>("packageHeaderAnalysisRule") {
    archiveClassifier.set("header-analysis-rule")
    from(sourceSets.main.get().output) {
        include("org/zaproxy/addon/reportingproxy/rules/HeaderAnalysisRule.class")
        // Include inner classes if any (like anonymous classes)
        include("org/zaproxy/addon/reportingproxy/rules/HeaderAnalysisRule$*.class")
    }
}

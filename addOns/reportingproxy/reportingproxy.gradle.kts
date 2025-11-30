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

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

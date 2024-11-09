nulltask("printDepsForJwin") {
    doLast {
        configurations.compileClasspath.get().forEach(::println)
    }
}
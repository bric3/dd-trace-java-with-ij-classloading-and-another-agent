plugins {
    java
}

tasks {
    jar {
        manifest {
            attributes(
                "Premain-Class" to "io.github.bric3.dd.anotherAgent.DumbAgent",
                // "Can-Redefine-Classes" to true,
                // "Can-Retransform-Classes" to true,
            )
        }
    }

    val agentJar by configurations.creating {
        isCanBeConsumed = true
        isCanBeResolved = false
    }

    artifacts {
        add(agentJar.name, jar.get().archiveFile) {
            builtBy(jar)
        }
    }
}
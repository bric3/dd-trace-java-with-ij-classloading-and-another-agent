plugins {
    `java-library`
}

// This project is used as an alternative for smoketest, in order to be a single gradle project for both the app and the other agent
description = "Check agent can be loaded with another agent on custom secure system class-loader"

repositories {
    mavenCentral()
}

val `ddAgent 1_23_0` by configurations.creating
val `ddAgent 1_24_0 fixed` by configurations.creating

dependencies {
    `ddAgent 1_23_0`("com.datadoghq:dd-java-agent:1.23.0")
    `ddAgent 1_24_0 fixed`("com.datadoghq:dd-java-agent:1.24.0")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.2")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.9.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.2")
    testImplementation("org.junit.platform:junit-platform-runner:1.9.2")
}

sourceSets {
    create("another-agent") {
        java
    }
}

tasks {
    jar {
        manifest {
            attributes("Main-Class" to "smoketest.app.App")
        }
    }

    val anotherAgentJar by registering(Jar::class) {
        archiveClassifier.set("another-agent")
        from(sourceSets["another-agent"].output)
        manifest {
            attributes("Premain-Class" to "smoketest.agent.AnotherAgent")
        }
    }

    withType<Test>().configureEach {
        dependsOn(jar, anotherAgentJar)
        useJUnitPlatform()

        systemProperties(
            "datadog.agent.1.23.0.path" to `ddAgent 1_23_0`.asPath,
            "datadog.agent.1.24.0.path" to `ddAgent 1_24_0 fixed`.asPath,
            "smoketest.jar.path" to jar.get().archiveFile.get().asFile.absolutePath,
            "smoketest.another.javaagent.path" to anotherAgentJar.get().archiveFile.get().asFile.absolutePath,
            "buildDir" to project.layout.buildDirectory.get().asFile.absolutePath,
        )
    }
}

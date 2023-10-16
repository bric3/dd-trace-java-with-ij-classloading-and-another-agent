plugins {
    java
    application
}

group = "io.github.bric3.dd"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}


val ddAgent by configurations.creating
val dumbAgent by configurations.creating {
    isCanBeResolved = true
    isCanBeConsumed = false
}

sourceSets.create("plugin") {
    java
}

dependencies {
    compileOnly("org.jetbrains:annotations:24.0.0")
    ddAgent("com.datadoghq:dd-java-agent:1.21.0")
    dumbAgent(project(
        path = ":dumb-agent",
        configuration = "agentJar",
    ))
}

application {
    mainClass = "io.github.bric3.dd.tracerWithNoCodeSourceClassLoader.DumbMain"
}

val run by tasks.getting(JavaExec::class) {
    inputs.files(ddAgent)
    inputs.files(dumbAgent)
    classpath(sourceSets["main"].runtimeClasspath)
    jvmArgs(
        "-Djava.system.class.loader=io.github.bric3.dd.tracerWithNoCodeSourceClassLoader.NoProtectionDomainClassLoader",
        "-javaagent:${dumbAgent.asPath}",
        "-javaagent:${ddAgent.asPath}",
    )
}

tasks.compileJava {
    dependsOn(tasks.named("compilePluginJava"))
}
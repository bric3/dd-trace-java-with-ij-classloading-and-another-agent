package io.github.bric3.dd.tracerWithNoCodeSourceClassLoader;

import java.util.Arrays;

public class DumbMain {
    public static void main(String[] args) {
        System.out.println("---- Args --------------------------------------------------------");
        ProcessHandle.current().info().arguments().stream().flatMap(Arrays::stream).forEach(System.out::println);
        System.out.println("------------------------------------------------------------------");

        ClassLoader classLoader = DumbMain.class.getClassLoader();
        if (!(classLoader instanceof NoProtectionDomainClassLoader)) {
            throw new RuntimeException("You must run JVM with -Djava.system.class.loader=io.github.bric3.dd.tracerWithNoCodeSourceClassLoader.NoProtectionDomainClassLoader");
        }

        System.out.println("Hello form app!");
    }
}

// > Task :modules:go:test
// ERROR datadog.trace.bootstrap.AgentBootstrap
// java.lang.IllegalStateException: Multiple javaagents specified and code source unavailable, not installing tracing agent
// CompileCommand: exclude com/intellij/openapi/vfs/impl/FilePartNodeRoot.trieDescend bool exclude = true
// 	at datadog.trace.bootstrap.AgentBootstrap.installAgentJar(AgentBootstrap.java:203)
// Could not get bootstrap jar from code source, using -javaagent arg
// 	at datadog.trace.bootstrap.AgentBootstrap.agentmain(AgentBootstrap.java:63)
// 	at datadog.trace.bootstrap.AgentBootstrap.premain(AgentBootstrap.java:54)
// 	at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
// 	at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:77)
// 	at java.base/jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
// 	at java.base/java.lang.reflect.Method.invoke(Method.java:568)
// 	at java.instrument/sun.instrument.InstrumentationImpl.loadClassAndStartAgent(InstrumentationImpl.java:491)
// 	at java.instrument/sun.instrument.InstrumentationImpl.loadClassAndCallPremain(InstrumentationImpl.java:503)
// WARNING: Unable to get VM args through reflection.  A custom java.util.logging.LogManager may not work correctly
// e: Supertypes of the following classes cannot be resolved. Please make sure you have the required dependencies in the classpath:
//     class kotlinx.coroutines.test.TestDispatcher, unresolved supertypes: kotlinx.coroutines.DelayWithTimeoutDiagnostics
// Adding -Xextended-compiler-checks argument might provide additional information.

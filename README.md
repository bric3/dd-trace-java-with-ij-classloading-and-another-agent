
# Reproducer for DD Tracer agent failing when code source is not available and multiple javaagents are specified

When developing IntelliJ plugins we potentially have multiple agents specified. 
For example, to help diagnose Kotlin's coroutines issues.

When regular classloading is used, this usually does not cause any issues.
However, when the system classloader defines classes (as it does in IntelliJ)
without a _protection domain_ the DD Tracer agent fails.

```text
> Task :modules:go:test
ERROR datadog.trace.bootstrap.AgentBootstrap
java.lang.IllegalStateException: Multiple javaagents specified and code source unavailable, not installing tracing agent
CompileCommand: exclude com/intellij/openapi/vfs/impl/FilePartNodeRoot.trieDescend bool exclude = true
	at datadog.trace.bootstrap.AgentBootstrap.installAgentJar(AgentBootstrap.java:203)
Could not get bootstrap jar from code source, using -javaagent arg
	at datadog.trace.bootstrap.AgentBootstrap.agentmain(AgentBootstrap.java:63)
	at datadog.trace.bootstrap.AgentBootstrap.premain(AgentBootstrap.java:54)
	at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
	at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:77)
	at java.base/jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
	at java.base/java.lang.reflect.Method.invoke(Method.java:568)
	at java.instrument/sun.instrument.InstrumentationImpl.loadClassAndStartAgent(InstrumentationImpl.java:491)
	at java.instrument/sun.instrument.InstrumentationImpl.loadClassAndCallPremain(InstrumentationImpl.java:503)
WARNING: Unable to get VM args through reflection.  A custom java.util.logging.LogManager may not work correctly
e: Supertypes of the following classes cannot be resolved. Please make sure you have the required dependencies in the classpath:
    class kotlinx.coroutines.test.TestDispatcher, unresolved supertypes: kotlinx.coroutines.DelayWithTimeoutDiagnostics
Adding -Xextended-compiler-checks argument might provide additional information.
```

This project has two subprojects that reproduce the issue.

* `demo-mimic-intellij` - This projects copied over the custom classloading system of IntelliJ, 
  which is used that way for various reasons, **plugin support** being one of them.
  ```bash
  ./gradlew :demo-mimic-intellij:run
  ```

   See [`DumbMain`](demo-mimic-intellij/src/main/java/io/github/bric3/dd/tracerWithIntelliJ/DumbMain.java),
   [IntelliJ's classloader `UrlClassLoader`](https://github.com/JetBrains/intellij-community/blob/ea502d196f4f2118e08de1fafc2295f1158f48ed/platform/util-class-loader/src/com/intellij/util/lang/UrlClassLoader.java#L284).


* `demo-basic` - This projects is a redux reproducer of what IntelliJ does, in particular it defines 
  classes without a _protection domain_.
  ```bash
  ./gradlew :demo-basic:run
  ```                    

  See [`DumbMain`](demo-basic/src/main/java/io/github/bric3/dd/tracerWithNoCodeSourceClassLoader/DumbMain.java),
  [`NoProtectionDomainClassLoader`](demo-basic/src/main/java/io/github/bric3/dd/tracerWithNoCodeSourceClassLoader/NoProtectionDomainClassLoader.java).

In both of these projects one can see
```text
Could not get bootstrap jar from code source, using -javaagent arg
WARNING: Unable to get VM args through reflection.  A custom java.util.logging.LogManager may not work correctly
ERROR datadog.trace.bootstrap.AgentBootstrap
java.lang.IllegalStateException: Multiple javaagents specified and code source unavailable, not installing tracing agent
        at datadog.trace.bootstrap.AgentBootstrap.installAgentJar(AgentBootstrap.java:203)
        at datadog.trace.bootstrap.AgentBootstrap.agentmain(AgentBootstrap.java:63)
        at datadog.trace.bootstrap.AgentBootstrap.premain(AgentBootstrap.java:54)
        at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:104)
        at java.base/java.lang.reflect.Method.invoke(Method.java:578)
        at java.instrument/sun.instrument.InstrumentationImpl.loadClassAndStartAgent(InstrumentationImpl.java:491)
        at java.instrument/sun.instrument.InstrumentationImpl.loadClassAndCallPremain(InstrumentationImpl.java:503)
```

The code responsible for this stack trace is in [`AgentBootstrap::installAgentJar`](https://github.com/DataDog/dd-trace-java/blob/ae085301598ca24f4e7c9ad546b2fc76313c2b7b/dd-java-agent/src/main/java/datadog/trace/bootstrap/AgentBootstrap.java#L168-L230).

## Possible solution

We could possibly make this work if the agent had a system property where we could indicate its location.

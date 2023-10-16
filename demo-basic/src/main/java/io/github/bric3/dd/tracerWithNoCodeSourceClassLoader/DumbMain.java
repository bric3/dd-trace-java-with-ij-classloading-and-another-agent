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

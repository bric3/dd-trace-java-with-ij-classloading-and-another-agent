package smoketest.app;

import smoketest.classloader.NoProtectionDomainClassLoader;

import java.util.Arrays;

public class App {
    public static void main(String[] args) {
        System.out.println("---- Args --------------------------------------------------------");
        ProcessHandle.current().info().arguments().stream().flatMap(Arrays::stream).forEach(System.out::println);
        System.out.println("------------------------------------------------------------------");

        ClassLoader classLoader = App.class.getClassLoader();
        if (!(classLoader instanceof NoProtectionDomainClassLoader)) {
            throw new RuntimeException("You must run JVM with -Djava.system.class.loader=smoketest.classloader.NoProtectionDomainClassLoader");
        }

        System.out.println("Hello form app!");
    }
}

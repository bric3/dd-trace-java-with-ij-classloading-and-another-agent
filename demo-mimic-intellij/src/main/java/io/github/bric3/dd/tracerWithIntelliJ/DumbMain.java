package io.github.bric3.dd.tracerWithIntelliJ;

import com.intellij.util.lang.PathClassLoader;
import com.intellij.util.lang.UrlClassLoader;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public class DumbMain {
    public static void main(String[] args) throws Throwable {
        System.out.println("---- Args --------------------------------------------------------");
        ProcessHandle.current().info().arguments().stream().flatMap(Arrays::stream).forEach(System.out::println);
        System.out.println("------------------------------------------------------------------");

        // try reproduce IntelliJ's 
        // com.intellij.idea.BootstrapClassLoaderUtil#initClassLoader
        ClassLoader classLoader = DumbMain.class.getClassLoader();
        if (!(classLoader instanceof PathClassLoader)) {
            throw new RuntimeException("You must run JVM with -Djava.system.class.loader=com.intellij.util.lang.PathClassLoader");
        }

        System.out.println("Hello form app!");

        UrlClassLoader urlClassLoader = UrlClassLoader.build()
                .files(List.of(Path.of("build/classes/java/plugin")))
                .get();

        Class<?> ddPlugin = urlClassLoader.loadClass("DDPlugin");
        ddPlugin.getDeclaredMethod("load").invoke(ddPlugin.getDeclaredConstructor().newInstance());
    }
}

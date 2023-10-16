package io.github.bric3.dd.tracerWithNoCodeSourceClassLoader;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.ProtectionDomain;
import java.util.Arrays;

/**
 * Simple class loader that defines classes without a protection domain (actually without
 * a CodeSource, in order to mimic IntelliJ's class loading behavior).
 *
 * @see <a href=""><code>com.intellij.util.lang.UrlClassLoader</code></a>
 */
@SuppressWarnings("unused")
public class NoProtectionDomainClassLoader extends URLClassLoader {
    private final Path[] classPath = Arrays.stream(
            System.getProperty("java.class.path").split(System.getProperty("path.separator"))
    ).map(Path::of).toArray(Path[]::new);

    /**
     * Used by system to inject the default class loader.
     *
     * @param parent
     * @see java.lang.ClassLoader#getSystemClassLoader
     */
    public NoProtectionDomainClassLoader(ClassLoader parent) {
        super(new URL[0], parent);
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        if (name.startsWith("io.github.bric3")) {
            return findClass(name);
        }
        return super.loadClass(name, resolve);
    }

    public Class<?> findClass(String name) throws ClassNotFoundException {

        String classFile = name.replace('.', '/') + ".class";
        try (InputStream classData = getResourceAsStream(classFile)) {
            byte[] array = readClassBytes(name, classData, classFile);

            // Since this class is used as the system class loader, we need to delegate
            // to the parent class loader, otherwise this class maybe loaded by again itself, thus
            // resulting in defining different classes making `instanceof` checks fail.
            if (name.endsWith(getClass().getSimpleName())) {
                return getParent().loadClass(name);
            }

            return defineClass(name, array, 0, array.length, (ProtectionDomain) null);
        } catch (IOException io) {
            throw new ClassNotFoundException("Loading class failed", io);
        }
    }

    private byte[] readClassBytes(String name, InputStream classData, String classFile) throws IOException, ClassNotFoundException {
        if (classData == null) {
            // try local classpath
            for (Path path : classPath) {
                Path resolvedPath = path.resolve(classFile);
                if (Files.exists(resolvedPath)) {
                    return Files.readAllBytes(resolvedPath);
                }
            }
            throw new ClassNotFoundException("Couldn't find class " + name);
        }

        return classData.readAllBytes();
    }

    /**
     * Called by the VM to support dynamic additions to the class path.
     *
     * @see java.lang.instrument.Instrumentation#appendToSystemClassLoaderSearch
     */
    @SuppressWarnings("unused")
    final void appendToClassPathForInstrumentation(@NotNull String jar) throws IOException {
        addURL(Path.of(jar).toRealPath().toFile().toURI().toURL());
    }
}

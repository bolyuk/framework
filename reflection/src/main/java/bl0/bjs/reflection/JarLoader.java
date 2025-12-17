package bl0.bjs.reflection;

import bl0.bjs.common.core.event.Event;
import bl0.bjs.common.core.tuple.Pair;
import bl0.bjs.logging.containers.ILogBatch;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class JarLoader {
    public static void addToClassLoader(URLClassLoader classLoader, String jarPath) {
        try {
            java.lang.reflect.Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
            method.setAccessible(true);
            method.invoke(classLoader, new File(jarPath).toURI().toURL());
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public static void parseClasses(URLClassLoader loader, String jarPath, ILogBatch batch, Event<Class<?>, Void> onLoaded, Event<Pair<String, Throwable>, Void> onFailed) {
        try (JarFile jarFile = new JarFile(new File(jarPath))) {
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry.getName().endsWith(".class") && !entry.getName().contains("module-info.class") && !entry.getName().startsWith("META-INF")) {
                    String className = entry.getName()
                            .replace("/", ".")
                            .replace(".class", "");
                    try {
                        Class<?> clazz = loader.loadClass(className);
                        onLoaded.invoke(clazz);
                    } catch (Throwable e) {
                        onFailed.invoke(Pair.of(className, e));
                    }
                }
            }
        } catch (Throwable e) {
            batch.err("Failed to preload Jar! skipped...", e);
        } finally {
            batch.flush();
        }
    }
}

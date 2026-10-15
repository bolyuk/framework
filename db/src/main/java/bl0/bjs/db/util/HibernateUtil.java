package bl0.bjs.db.util;

import jakarta.persistence.Entity;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.reflections.Reflections;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class HibernateUtil {

    public static SessionFactory buildSessionFactory(String host, String dbName, String user, String password, String... entitiesPaths) {
        try {
            Map<String, Object> settings = Map.of(
                    "hibernate.connection.driver_class", "org.mariadb.jdbc.Driver",
                    "hibernate.connection.url", "jdbc:mariadb://"+host+"/"+dbName,
                    "hibernate.connection.username", user,
                    "hibernate.connection.password", password,
                    "hibernate.hbm2ddl.auto", "update"
            );

            StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
                    .applySettings(settings)
                    .build();

            MetadataSources sources = new MetadataSources(registry);
            for (Class<?> entity : scanEntityClasses(entitiesPaths)) {
                sources.addAnnotatedClass(entity);
            }

            return sources.buildMetadata().buildSessionFactory();
        } catch (Exception e) {
            throw new RuntimeException("Failed to build SessionFactory", e);
        }
    }

    public static SessionFactory buildSQLiteSessionFactory(
            String databaseFile,
            String... entitiesPaths
    ) {
        try {

            Map<String, Object> settings = Map.of(
                    "hibernate.connection.driver_class", "org.sqlite.JDBC",

                    "hibernate.connection.url",
                    "jdbc:sqlite:" + databaseFile,

                    "hibernate.dialect",
                    "org.hibernate.community.dialect.SQLiteDialect",

                    "hibernate.hbm2ddl.auto", "update",

                    // recommended for sqlite
                    "hibernate.show_sql", false,
                    "hibernate.format_sql", false,

                    // sqlite concurrency quirks™
                    "hibernate.connection.pool_size", 1
            );

            StandardServiceRegistry registry =
                    new StandardServiceRegistryBuilder()
                            .applySettings(settings)
                            .build();

            MetadataSources sources = new MetadataSources(registry);
            for (Class<?> entity : scanEntityClasses(entitiesPaths)) {
                sources.addAnnotatedClass(entity);
            }

            return sources
                    .buildMetadata()
                    .buildSessionFactory();

        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to build SQLite SessionFactory",
                    e
            );
        }
    }

    /** Scans every given package root for @Entity classes and merges the results (deduped, in case
     *  two roots overlap). An app's own entities typically live in one package, but a shared
     *  library (e.g. aeon-framework's generic auth entities) contributes its own entities from a
     *  completely different package root - this is why a single entitiesPath was never enough once
     *  more than one source of entities existed on the classpath. */
    private static Set<Class<?>> scanEntityClasses(String... entitiesPaths) {
        Set<Class<?>> entities = new LinkedHashSet<>();
        for (String path : entitiesPaths) {
            entities.addAll(new Reflections(path).getTypesAnnotatedWith(Entity.class));
        }
        return entities;
    }
}

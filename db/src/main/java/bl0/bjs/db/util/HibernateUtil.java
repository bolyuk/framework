package bl0.bjs.db.util;

import jakarta.persistence.Entity;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.reflections.Reflections;

import java.util.Map;

public class HibernateUtil {

    public static SessionFactory buildSessionFactory(String host, String dbName, String user, String password ) {
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

            Reflections reflections = new Reflections("org.bl0.bjs.maidbot.entities.data");
            var entities = reflections.getTypesAnnotatedWith(Entity.class);
            for (Class<?> entity : entities) {
                sources.addAnnotatedClass(entity);
            }

            return sources.buildMetadata().buildSessionFactory();
        } catch (Exception e) {
            throw new RuntimeException("Failed to build SessionFactory", e);
        }
    }
}

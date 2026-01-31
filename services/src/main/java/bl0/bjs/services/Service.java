package bl0.bjs.services;

import bl0.bjs.services.interfaces.IService;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Service {
    Class<? extends IService>[] exportServices();

    boolean isSingleton() default true;

    boolean isAutoBindingEnabled() default true;
}

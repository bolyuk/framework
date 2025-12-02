package bl0.bjs.common.base.service;

import bl0.bjs.common.base.service.interfaces.IService;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Service {
    Class<? extends IService> exportService();
    boolean isSingelton() default true;
    boolean isAutoBindingEnabled() default true;
}

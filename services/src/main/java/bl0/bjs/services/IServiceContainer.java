package bl0.bjs.services;

import bl0.bjs.services.interfaces.IService;

public interface IServiceContainer {
    <T extends IService> T getService(Class<T> serviceClass);

    boolean registerService(Class<?> serviceClass);

    void unregisterService(Class<?> serviceClass);

    void registerSingelton(Object instance, Class<? extends IService> serviceInterface);
}

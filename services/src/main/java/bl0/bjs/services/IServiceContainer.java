package bl0.bjs.services;

import bl0.bjs.services.interfaces.IService;

public interface IServiceContainer {
    public <T extends IService> T getService(Class<T> serviceClass);

    public boolean registerService(Class<?> serviceClass);

    public void unregisterService(Class<?> serviceClass);

    public void registerSingelton(Object instance, Class<? extends IService> serviceInterface);
}

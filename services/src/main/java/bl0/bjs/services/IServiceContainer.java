package bl0.bjs.services;

import bl0.bjs.services.interfaces.IService;

import java.util.List;

public interface IServiceContainer {
    <T extends IService> T get(Class<T> serviceClass);

    <T extends IService> boolean add(Class<T> serviceClass);

    void addSingleton(Object instance);

    List<? extends Class<? extends IService>> getAllServices();

    void addExtender(IServiceExtender extender);

    void removeExtender(IServiceExtender extender);
}

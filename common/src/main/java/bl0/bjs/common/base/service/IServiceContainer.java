package bl0.bjs.common.base.service;

import bl0.bjs.common.base.service.interfaces.IService;
import bl0.bjs.common.core.logging.containers.ILogBatch;

public interface IServiceContainer {
    public <T extends IService> T getService(Class<T> serviceClass);

    public boolean registerService(Class<?> serviceClass, ILogBatch batch);

    public void unregisterService(Class<?> serviceClass);

    public void registerSingelton(Object instance, Class<? extends IService> serviceInterface);
}

package bl0.bjs.services;

import bl0.bjs.services.interfaces.IService;

public interface IServiceExtender {
    <T extends IService> T find(Class<T> serviceClass);
}

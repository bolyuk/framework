package bl0.bjs.common.base;

import bl0.bjs.eventbus.IEventBus;
import bl0.bjs.services.IServiceContainer;
import bl0.bjs.logging.ILogger;

public interface IContext {

    public IEventBus getEventBus();

    public IServiceContainer getServiceContainer();

    public ILogger generateLogger(Class<?> clazz);

    public String getHostname();

}

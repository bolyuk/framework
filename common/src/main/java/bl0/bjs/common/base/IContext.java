package bl0.bjs.common.base;

import bl0.bjs.common.async.control.IAsyncBus;
import bl0.bjs.eventbus.IEventBus;
import bl0.bjs.logging.ILogger;
import bl0.bjs.services.IServiceContainer;

public interface IContext {

    IEventBus getEventBus();

    IServiceContainer getServiceContainer();

    ILogger generateLogger(Class<?> clazz);

    String getHostname();

    IAsyncBus getAsyncBus();

}

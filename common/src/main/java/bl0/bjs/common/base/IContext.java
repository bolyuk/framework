package bl0.bjs.common.base;

import bl0.bjs.common.base.eventbus.IEventBus;
import bl0.bjs.common.base.service.IServiceContainer;
import bl0.bjs.common.core.logging.ILogger;

public interface IContext {

    public IEventBus getEventBus();

    public IServiceContainer getServiceHelper();

    public ILogger generateLogger(Class<?> clazz);

}

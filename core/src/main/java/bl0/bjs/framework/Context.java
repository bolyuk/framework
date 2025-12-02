package bl0.bjs.framework;

import bl0.bjs.eventbus.IEventBus;
import bl0.bjs.common.base.IContext;
import bl0.bjs.services.IServiceContainer;
import bl0.bjs.logging.ILogger;
import bl0.bjs.framework.eventbus.EventBus;
import bl0.bjs.framework.files.LocalStorage;
import bl0.bjs.framework.logging.DefaultLogger;

public class Context implements IContext {

    public final EventBus eventBus = new EventBus();
    public final ServiceContainer services;
    public final LocalStorage localStorage;

    public Context(LocalStorage localStorage) {
        this.localStorage = localStorage;
        this.services = new ServiceContainer(this);
    }

    @Override
    public IEventBus getEventBus() {
        return eventBus;
    }

    @Override
    public IServiceContainer getServiceContainer() {
        return services;
    }

    @Override
    public ILogger generateLogger(Class<?> clazz) {
        return getLoggerIntern(clazz);
    }


    public DefaultLogger getLoggerIntern(Class<?> binding) {
        return new DefaultLogger(this, binding);
    }
}

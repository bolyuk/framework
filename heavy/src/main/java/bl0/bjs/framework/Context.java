package bl0.bjs.framework;

import org.bl0.framework.common.base.eventbus.IEventBus;
import org.bl0.framework.common.base.IContext;
import org.bl0.framework.common.base.service.IServiceContainer;
import org.bl0.framework.common.core.logging.ILogger;
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
    public IServiceContainer getServiceHelper() {
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

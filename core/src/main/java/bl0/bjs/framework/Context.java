package bl0.bjs.framework;

import bl0.bjs.async.control.SoftThreadBus;
import bl0.bjs.common.async.control.IAsyncBus;
import bl0.bjs.eventbus.IEventBus;
import bl0.bjs.common.base.IContext;
import bl0.bjs.services.IServiceContainer;
import bl0.bjs.logging.ILogger;
import bl0.bjs.framework.eventbus.EventBus;
import bl0.bjs.framework.files.LocalStorage;
import bl0.bjs.framework.logging.DefaultLogger;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class Context implements IContext {
    public final ConcurrentHashMap<Class<?>, Object> extraData = new ConcurrentHashMap<>();

    public final EventBus eventBus = new EventBus();
    public final ServiceContainer services;
    public final LocalStorage localStorage;
    public final String hostname;

    public final IAsyncBus asyncBus;

    public Context(LocalStorage localStorage, String hostname) {
        this.hostname = hostname;
        this.localStorage = localStorage;
        this.services = new ServiceContainer(this);
        this.asyncBus = new SoftThreadBus(this);
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

    @Override
    public String getHostname() {
        return hostname;
    }

    @Override
    public IAsyncBus getAsyncBus() {
        return asyncBus;
    }

    @Override
    public void saveData(Object data) {
        extraData.put(data.getClass(), data);
    }

    @Override
    public <T> T getData(Class<T> clazz) {
        var data = extraData.getOrDefault(clazz, null);
        if(data == null)
            throw new IllegalArgumentException(String.format("No such class %s", clazz.getName()));
        return (T) data;
    }


    public DefaultLogger getLoggerIntern(Class<?> binding) {
        return new DefaultLogger(this, binding);
    }
}

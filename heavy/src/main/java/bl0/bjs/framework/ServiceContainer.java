package bl0.bjs.framework;

import org.bl0.framework.common.base.service.IServiceContainer;
import org.bl0.framework.common.base.service.Service;
import org.bl0.framework.common.base.service.interfaces.IService;
import org.bl0.framework.common.core.logging.containers.ILogBatch;
import org.bl0.framework.common.sys.events.ServiceEvent;
import bl0.bjs.framework.logging.DefaultLogger;
import bl0.bjs.framework.utils.ClassUtils;

import java.util.concurrent.ConcurrentHashMap;

public class ServiceContainer implements IServiceContainer {
    private final Context context;
    private final DefaultLogger l;
    private final ConcurrentHashMap<Class<? extends IService>, Class<?>> services = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Class<? extends IService>, Object> singletons = new ConcurrentHashMap<>();

    public ServiceContainer(Context context) {
        this.context = context;
        this.l = context.getLoggerIntern(ServiceContainer.class);
    }

    public <T extends IService> T getService(Class<T> serviceClass) {
        Class<?> result = services.get(serviceClass);
        if (result == null) {
            l.err("No service found for: " + serviceClass.getName());
            return null;
        }

        Service annotation = result.getAnnotation(Service.class);

        try {
            if (annotation.isSingelton()) {
                Object instance = singletons.get(serviceClass);
                if (instance == null) {
                    l.err("No singleton found for: " + serviceClass.getName());
                    return null;
                }
                return (T) instance;
            }
            return serviceClass.cast(result.getDeclaredConstructor().newInstance());
        } catch (Exception e) {
            l.err("Failed to instantiate service " + serviceClass.getName(), e);
            return null;
        }
    }

    @Override
    public boolean registerService(Class<?> serviceClass, ILogBatch batch) {
        l.genLocalBatchIfNull(batch);

        Service annotation = serviceClass.getAnnotation(Service.class);
        if (annotation == null) {
            return false;
        }

        if (!IService.class.isAssignableFrom(serviceClass)) {
            batch.err("Service " + serviceClass.getName() + " is not implements IService.class ");
            return false;
        }

        if (!annotation.isAutoBindingEnabled()) {
            batch.log(serviceClass.getName() + " AutoBinding is disabled, skipped");
            return false;
        }

        if (services.containsKey(annotation.exportService())) {
            batch.err("Service already registered: " + annotation.exportService());
            return false;
        }

        if (!annotation.exportService().isAssignableFrom(serviceClass)) {
            batch.err("Service is not child class of exporting interface: " + annotation.exportService());
            return false;
        }

        try {
            if (!ClassUtils.isSubclassOf(serviceClass, IService.class)) {
                serviceClass.getDeclaredConstructor();
            }
        } catch (Exception e) {
            batch.err("No constructor without arguments found for: " + serviceClass.getName());
            return false;
        }
        services.put(annotation.exportService(), serviceClass);
        context.getEventBus().getController(ServiceEvent.class).fireEvent(new ServiceEvent.ServicePayload((Class<? extends IService>) serviceClass, ServiceEvent.EventType.REGISTERED));
        batch.log("Registered: " + serviceClass.getName() + " for service " + annotation.exportService().getName());
        batch.flushIfLocal();
        return true;
    }

    @Override
    public void unregisterService(Class<?> serviceClass) {

    }

    @Override
    public void registerSingelton(Object instance, Class<? extends IService> serviceInterface) {
        if (!services.containsKey(serviceInterface)) {
            services.put(serviceInterface, instance.getClass());
        }
        if (!serviceInterface.isAssignableFrom(instance.getClass()))
            throw new IllegalArgumentException("Instance class must be of type " + serviceInterface.getName());

        singletons.put(serviceInterface, instance);
    }
}

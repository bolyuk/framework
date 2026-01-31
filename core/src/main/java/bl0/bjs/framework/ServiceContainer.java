package bl0.bjs.framework;

import bl0.bjs.common.base.BJSBaseClass;
import bl0.bjs.common.base.IContext;
import bl0.bjs.framework.logging.DefaultLogger;
import bl0.bjs.logging.containers.ILogBatch;
import bl0.bjs.services.IServiceContainer;
import bl0.bjs.services.IServiceExtender;
import bl0.bjs.services.Service;
import bl0.bjs.services.events.ServiceEvent;
import bl0.bjs.services.interfaces.IService;

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ServiceContainer implements IServiceContainer {

    private final Context context;
    private final DefaultLogger l;

    private final ConcurrentHashMap<Class<? extends IService>, Entry> services = new ConcurrentHashMap<>();

    private final ConcurrentLinkedQueue<IServiceExtender> extenders = new ConcurrentLinkedQueue<>();

    public ServiceContainer(Context context) {
        this.context = Objects.requireNonNull(context, "context");
        this.l = context.getLoggerIntern(ServiceContainer.class);
    }

    @Override
    public <T extends IService> T get(Class<T> serviceClass) {
        Objects.requireNonNull(serviceClass, "serviceClass");

        Entry entry = services.get(serviceClass);
        if (entry != null) {
            try {
                return serviceClass.cast(entry.get(context));
            } catch (Exception e) {
                l.err("Error trying to retrieve instance for service: " + serviceClass.getName(), e);
                return null;
            }
        }

        T extended = findInExtenders(serviceClass);
        if (extended != null) return extended;

        l.err("No instance found for service: " + serviceClass.getName());
        return null;
    }

    @Override
    public <T extends IService> boolean add(Class<T> implClass) {
        Objects.requireNonNull(implClass, "implClass");
        ILogBatch batch = l.genBatch();

        Service ann = implClass.getAnnotation(Service.class);
        if (ann == null) {
            batch.err("Class " + implClass.getName() + " has no @Service annotation");
            batch.flushAndClear();
            return false;
        }

        if (!ann.isAutoBindingEnabled()) {
            batch.log(implClass.getName() + " auto binding disabled, skipped");
            batch.flushAndClear();
            return false;
        }

        Class<? extends IService>[] exported = ann.exportServices();
        if (exported == null || exported.length == 0) {
            batch.err("@Service(exportServices=...) is empty for " + implClass.getName());
            batch.flushAndClear();
            return false;
        }

        for (Class<? extends IService> contract : exported) {
            if (contract == null) {
                batch.err("Null exportService in " + implClass.getName());
                batch.flushAndClear();
                return false;
            }
            if (!contract.isAssignableFrom(implClass)) {
                batch.err("Class [" + implClass.getName() + "] is not assignable to exported contract: " + contract.getName());
                batch.flushAndClear();
                return false;
            }
        }

        Object singletonInstance = null;
        if (ann.isSingleton()) {
            try {
                singletonInstance = instantiate(implClass, context);
            } catch (Exception e) {
                batch.err("Error trying to instantiate singleton service: " + implClass.getName(), e);
                batch.flushAndClear();
                return false;
            }
        }

        Entry entry = new Entry(implClass, singletonInstance);

        for (Class<? extends IService> contract : exported) {
            Entry prev = services.putIfAbsent(contract, entry);
            if (prev != null) {
                batch.err("Implementation already registered for service: " + contract.getName()
                        + " (existing=" + prev.implClass.getName() + ", new=" + implClass.getName() + ")");
                for (Class<? extends IService> c2 : exported) {
                    services.remove(c2, entry);
                    if (c2 == contract) break;
                }
                batch.flushAndClear();
                return false;
            }
        }

        context.getEventBus().getController(ServiceEvent.class)
                .fireEvent(new ServiceEvent.ServicePayload(implClass, ServiceEvent.EventType.REGISTERED));

        batch.log("Registered: " + implClass.getSimpleName() + " for service(s): " + joinContracts(exported));
        batch.flushAndClear();
        return true;
    }

    @Override
    public void addSingleton(Object instance) {
        Objects.requireNonNull(instance, "instance");
        ILogBatch batch = l.genBatch();

        if (!(instance instanceof IService)) {
            batch.err("addSingleton: instance " + instance.getClass().getName() + " does not implement IService");
            batch.flushAndClear();
            throw new IllegalArgumentException("Instance must implement IService");
        }

        Class<?> implClass = instance.getClass();
        Service ann = implClass.getAnnotation(Service.class);

        Set<Class<? extends IService>> contracts = new LinkedHashSet<>();

        if (ann != null && ann.exportServices() != null && ann.exportServices().length > 0) {
            contracts.addAll(Arrays.asList(ann.exportServices()));
        } else {
            for (Class<?> itf : implClass.getInterfaces()) {
                if (IService.class.isAssignableFrom(itf)) {
                    @SuppressWarnings("unchecked")
                    Class<? extends IService> c = (Class<? extends IService>) itf;
                    contracts.add(c);
                }
            }
        }

        if (contracts.isEmpty()) {
            batch.err("addSingleton: no IService contracts found for " + implClass.getName());
            batch.flushAndClear();
            throw new IllegalStateException("No IService interfaces to register singleton under");
        }

        Entry entry = new Entry((Class<? extends IService>) implClass, instance);

        for (Class<? extends IService> contract : contracts) {
            services.put(contract, entry); // explicit override for singletons is often desired
        }

        batch.log("Registered singleton: " + implClass.getSimpleName() + " for service(s): " + joinContracts(contracts.toArray(new Class[0])));
        batch.flushAndClear();
    }

    @Override
    public List<? extends Class<? extends IService>> getAllServices() {
        HashSet<Class<? extends IService>> impls = new HashSet<>();
        for (Entry e : services.values()) {
            impls.add(e.implClass);
        }
        return impls.stream().toList();
    }

    @Override
    public void addExtender(IServiceExtender extender) {
        extenders.add(Objects.requireNonNull(extender, "extender"));
    }

    @Override
    public void removeExtender(IServiceExtender extender) {
        extenders.remove(extender);
    }

    private <T extends IService> T findInExtenders(Class<T> serviceClass) {
        for (IServiceExtender extender : extenders) {
            T value = extender.find(serviceClass);
            if (value != null) return value;
        }
        return null;
    }

    private String joinContracts(Class<? extends IService>[] exported) {
        StringBuilder sb = new StringBuilder();
        for (Class<? extends IService> c : exported) {
            sb.append(c.getSimpleName()).append(", ");
        }
        if (sb.length() >= 2) sb.setLength(sb.length() - 2);
        return sb.toString();
    }

    private IService instantiate(Class<? extends IService> implClass, IContext ctx) throws Exception {
        if (BJSBaseClass.class.isAssignableFrom(implClass)) {
            Constructor<? extends IService> ctor = implClass.getConstructor(IContext.class);
            return ctor.newInstance(ctx);
        }
        Constructor<? extends IService> ctor = implClass.getConstructor();
        return ctor.newInstance();
    }

    private record Entry(Class<? extends IService> implClass, Object singleton) {

        public IService get(IContext ctx) throws Exception {
            if (singleton != null) return (IService) singleton;

            if (BJSBaseClass.class.isAssignableFrom(implClass)) {
                return implClass.getConstructor(IContext.class).newInstance(ctx);
            }
            return implClass.getConstructor().newInstance();
        }
    }
}

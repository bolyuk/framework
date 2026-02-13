package bl0.bjs.framework.eventbus;

import bl0.bjs.common.base.BJSBaseClass;
import bl0.bjs.common.base.IContext;
import bl0.bjs.eventbus.IEventBus;
import bl0.bjs.eventbus.IEventBusController;
import bl0.bjs.eventbus.IEventBusNode;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class EventBus implements IEventBus {
    private final ConcurrentHashMap<Class<? extends IEventBusNode<?>>, ConcurrentLinkedQueue<IEventBusNode<?>>> nodes = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Class<? extends IEventBusNode<?>>, IEventBusController<? extends IEventBusNode<?>, ?>> controllers = new ConcurrentHashMap<>();

    @Override
    public <T extends IEventBusNode<R>, R> IEventBusController<T, R> getController(Class<T> clazz) {
        return (IEventBusController<T, R>) controllers.computeIfAbsent(
                clazz,
                c -> new EventBusController<>(this, clazz)
        );
    }

    public ConcurrentLinkedQueue<IEventBusNode<?>> getNode(Class<? extends IEventBusNode<?>> clazz) {
        return nodes.computeIfAbsent(clazz, c -> new ConcurrentLinkedQueue<>());
    }
}

package bl0.bjs.framework.eventbus;

import org.bl0.framework.common.base.eventbus.IEventBus;
import org.bl0.framework.common.base.eventbus.IEventBusController;
import org.bl0.framework.common.base.eventbus.IEventBusNode;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class EventBus implements IEventBus {
    private final ConcurrentHashMap<Class<? extends IEventBusNode<?>>, ConcurrentLinkedQueue<IEventBusNode<?>>> nodes = new ConcurrentHashMap<>();

    @Override
    public <T extends IEventBusNode<R>, R> IEventBusController<T, R> getController(Class<T> clazz) {
        return new EventBusController<T, R>(this, clazz);
    }

    public ConcurrentLinkedQueue<IEventBusNode<?>> getNode(Class<? extends IEventBusNode<?>> clazz) {
        nodes.putIfAbsent(clazz, new ConcurrentLinkedQueue<>());
        return nodes.get(clazz);
    }
}

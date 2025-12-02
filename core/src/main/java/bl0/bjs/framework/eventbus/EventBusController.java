package bl0.bjs.framework.eventbus;

import bl0.bjs.eventbus.IEventBusController;
import bl0.bjs.eventbus.IEventBusNode;

public class EventBusController<T extends IEventBusNode<R>,R> implements IEventBusController<T, R> {
    private final EventBus eventBus;
    private final Class<T> clazzT;

    public EventBusController(EventBus eventBus, Class<T> clazzT) {
        this.eventBus = eventBus;
        this.clazzT = clazzT;
    }

    @Override
    public void subscribe(T node) {
        eventBus.getNode(clazzT).add(node);
    }

    @Override
    public void unsubscribe(T node) {
        eventBus.getNode(clazzT).remove(node);
    }

    @Override
    public void fireEvent(R data) {
        eventBus.getNode(clazzT).forEach(a -> ((IEventBusNode<R>)a).onEvent(data));
    }
}

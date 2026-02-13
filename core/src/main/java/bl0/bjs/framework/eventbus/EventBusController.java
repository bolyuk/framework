package bl0.bjs.framework.eventbus;

import bl0.bjs.async.queue.Queue;
import bl0.bjs.common.base.BJSBaseClass;
import bl0.bjs.common.base.IContext;
import bl0.bjs.eventbus.IEventBusController;
import bl0.bjs.eventbus.IEventBusNode;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class EventBusController<T extends IEventBusNode<R>,R> implements IEventBusController<T, R> {
    private final Queue<R> queue;
    private final ConcurrentLinkedQueue<IEventBusNode<?>> node;

    public EventBusController(EventBus eventBus, Class<T> clazzT) {
        this.queue = new Queue<>(this::qFunction);
        this.queue.setMaxBatchSize(1);
        this.node = eventBus.getNode(clazzT);
    }

    private void qFunction(Queue<R> rQueue, List<R> rs) {
        var v = rs.get(0);
        node.forEach(a -> ((IEventBusNode<R>)a).onEvent(v));
    }

    @Override
    public void subscribe(T node) {
        this.node.add(node);
    }

    @Override
    public void subscribeGeneric(IEventBusNode<R> node) {
        this.node.add(node);
    }

    @Override
    public void unsubscribe(T node) {
        this.node.remove(node);
    }

    @Override
    public void fireEvent(R data) {
        queue.pass(data);
    }
}

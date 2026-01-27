package bl0.bjs.async.queue;

import bl0.bjs.common.base.BJSBaseClass;
import bl0.bjs.common.base.IContext;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class QueuePool<ID, R, T extends Queue<R>> extends BJSBaseClass {
    private final ConcurrentHashMap<ID, T> queuePool = new ConcurrentHashMap<>();

    private final Function<ID, T> queueFactory;
    private boolean autoGenerateQueue = true;

    private int batchMaxSize = -1;

    private int startDelayMillis = 0;

    public QueuePool(IContext ctx, Function<ID, T> queueFactory) {
        super(ctx);
        this.queueFactory = queueFactory;
    }

    public void allowAutoGenerateQueue(boolean value) {
        this.autoGenerateQueue = value;
    }

    public void setMaxBatchSize(int value) {
        batchMaxSize = value;
    }

    public void setStartDelayMillis(int value) {
        startDelayMillis = value;
    }

    public void pass(ID id, List<R> values) {
        internalPass(id, values);
    }

    public void pass(ID id, R value) {
        internalPass(id, List.of(value));
    }

    @SafeVarargs
    public final void pass(ID id, R... values) {
        internalPass(id, List.of(values));
    }

    private void internalPass(ID id, List<R> values) {
        T queue = resolveQueue(id);
        queue.setMaxBatchSize(batchMaxSize);
        queue.setStartDelayMillis(startDelayMillis);
        queue.pass(values);
    }

    private T resolveQueue(ID id) {
        if (!autoGenerateQueue) {
            T q = queuePool.get(id);
            if (q == null) throw new IllegalStateException("Queue with id [" + id + "] not found");
            return q;
        }
        return queuePool.computeIfAbsent(id, queueFactory);
    }

    public boolean passIf(ID id, R value, java.util.function.Predicate<List<R>> allow) {
        T q = resolveQueue(id);
        q.setMaxBatchSize(batchMaxSize);
        q.setStartDelayMillis(startDelayMillis);
        return q.passIf(List.of(value), allow);
    }
}

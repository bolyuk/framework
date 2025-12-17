package bl0.bjs.async.queue;

import bl0.bjs.common.async.queue.IQueue;
import bl0.bjs.common.base.BJSBaseClass;
import bl0.bjs.common.base.IContext;

import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

public class QueuePool<ID, R, T extends IQueue<R>> extends BJSBaseClass {
    private final HashMap<ID, T> queuePool = new HashMap<>();

    private final Function<ID, T> queueFactory;
    private boolean autoGenerateQueue = true;

    public QueuePool(IContext ctx, Function<ID, T> queueFactory) {
        super(ctx);
        this.queueFactory = queueFactory;
    }

    public void allowAutoGenerateQueue(boolean value){
        this.autoGenerateQueue = value;
    }

    public void pass(ID id, List<R> values){
        internalPass(id, values);
    }

    public void pass(ID id, R value){
        internalPass(id, List.of(value));
    }

    @SafeVarargs
    public final void pass(ID id, R... values){
        internalPass(id, List.of(values));
    }

    private void internalPass(ID id, List<R> values){
        T queue = resolveQueue(id);
        queue.pass(values);
    }

    private T resolveQueue(ID id){
        T queue = queuePool.get(id);
        if(queue == null){
            if(!autoGenerateQueue)
                throw new IllegalStateException("Queue with id ["+id+"] not found");
            queue = queueFactory.apply(id);
            queuePool.put(id, queue);
        }
        return queue;
    }
}

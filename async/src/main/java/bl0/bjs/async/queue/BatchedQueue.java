package bl0.bjs.async.queue;

import bl0.bjs.common.base.IContext;

import java.util.List;
import java.util.function.BiConsumer;

public class BatchedQueue<T> extends BaseQueue<List<T>>  {

    protected int maxBatchSize;

    public BatchedQueue(IContext ctx, BiConsumer<BatchedQueue<T>, List<T>> queueFunction) {
        super(ctx, queueFunction);
    }


    public void setMaxBatchSize(int value) {
        synchronized (lock) {
            this.maxBatchSize = value;
        }
    }
}

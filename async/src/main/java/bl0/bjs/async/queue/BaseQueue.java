package bl0.bjs.async.queue;

import bl0.bjs.common.async.queue.IQueue;
import bl0.bjs.common.base.BJSBaseClass;
import bl0.bjs.common.base.IContext;
import bl0.bjs.async.AsyncExecutor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.BiConsumer;

public class BaseQueue<T> extends BJSBaseClass implements IQueue<T> {
    protected final ConcurrentLinkedDeque<T> data = new ConcurrentLinkedDeque<>();
    protected final BiConsumer<BaseQueue<T>, T> queueFunction;
    protected final Object lock = new Object();
    protected boolean processing = false;

    // --- delay support ---
    private static final ScheduledExecutorService DELAY_EXECUTOR =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "QueueDelayScheduler");
                t.setDaemon(true);
                return t;
            });

    private long startDelayMillis = 0;
    private ScheduledFuture<?> delayedStartFuture;

    public BaseQueue(IContext ctx, BiConsumer<BaseQueue<T>, T> queueFunction) {
        super(ctx);
        this.queueFunction = queueFunction;
    }


    public void setStartDelayMillis(long delayMillis) {
        synchronized (lock) {
            this.startDelayMillis = Math.max(delayMillis, 0);
        }
    }

    public void pass(List<T> values){
        synchronized (lock) {
            data.addAll(values);

            if (processing)
                return;

            if (startDelayMillis <= 0) {
                processing = true;
                AsyncExecutor.register(this::internalWork);
            } else
                scheduleDelayedStart();
        }
    }

    private void scheduleDelayedStart() {
        if (delayedStartFuture != null && !delayedStartFuture.isDone()) {
            delayedStartFuture.cancel(false);
        }

        delayedStartFuture = DELAY_EXECUTOR.schedule(() -> {
            synchronized (lock) {
                if (processing || data.isEmpty()) {
                    return;
                }
                processing = true;
            }
            AsyncExecutor.register(this::internalWork);
        }, startDelayMillis, TimeUnit.MILLISECONDS);
    }

    private void internalWork(){
        while (true) {
            if (data.isEmpty()) {
                processing = false;
                return;
            }
            try {
                accept(this, data.poll());
            } catch (Exception e) {
                l.err("Error processing queue: ", e);
            }
        }
    }

    protected void accept(BaseQueue<T> q, T batch){
        queueFunction.accept(q, batch);
    }
}

package bl0.bjs.async.queue;

import bl0.bjs.common.base.BJSBaseClass;
import bl0.bjs.common.base.IContext;
import bl0.bjs.async.AsyncExecutor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.BiConsumer;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class Queue<T> extends BJSBaseClass {
    private final ArrayList<T> data = new ArrayList<>();
    private final BiConsumer<Queue<T>, List<T>> queueFunction;
    private final Object lock = new Object();
    private boolean processing = false;
    private int maxBatchSize = -1;

    // --- delay support ---
    private static final ScheduledExecutorService DELAY_EXECUTOR =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "QueueDelayScheduler");
                t.setDaemon(true);
                return t;
            });

    private long startDelayMillis = 0;
    private ScheduledFuture<?> delayedStartFuture;

    public Queue(IContext ctx, BiConsumer<Queue<T>, List<T>> queueFunction) {
        super(ctx);
        this.queueFunction = queueFunction;
    }

    public void setMaxBatchSize(int value) {
        synchronized (lock) {
            this.maxBatchSize = value;
        }
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

    public boolean isEmpty(){
        synchronized (lock) {
            return data.isEmpty();
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
            List<T> batch;

            synchronized (lock) {
                if (data.isEmpty()) {
                    processing = false;
                    return;
                }
                if(maxBatchSize == -1){
                    batch = new ArrayList<>(data);
                    data.clear();
                } else {
                    int batchSize = Math.min(maxBatchSize, data.size());
                    batch = new ArrayList<>(data.subList(0, batchSize));
                    data.subList(0, batchSize).clear();
                }

            }
            try {
                accept(this, batch);
            } catch (Exception e) {
                l.err("Error processing queue: ", e);
            }
        }
    }

    protected void accept(Queue<T> q, List<T> batch){
        queueFunction.accept(q, batch);
    }
}

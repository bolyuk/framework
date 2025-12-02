package bl0.bjs.common.core.async;

import bl0.bjs.common.base.BJSBaseClass;
import bl0.bjs.common.base.IContext;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class BatchWorker extends BJSBaseClass {

    public BatchWorker(IContext ctx) {
        super(ctx);
    }
    public <T> void forEach(List<T> items, Consumer<T> worker) {
        if (items == null || items.isEmpty())
            return;

        CountDownLatch latch = new CountDownLatch(items.size());

        for (T item : items) {
            AsyncExecutor.register(() -> {
                try {
                    worker.accept(item);
                } catch (Exception e) {
                    l.err("Error in BatchWorker.forEach: ", e);
                } finally {
                    latch.countDown();
                }
            });
        }

        await(latch);
    }

    public <T> void forEachIndexed(List<T> items, BiConsumer<T, Integer> worker) {
        if (items == null || items.isEmpty())
            return;

        CountDownLatch latch = new CountDownLatch(items.size());

        for (int i = 0; i < items.size(); i++) {
            final int index = i;
            final T item = items.get(i);

            AsyncExecutor.register(() -> {
                try {
                    worker.accept(item, index);
                } catch (Exception e) {
                    l.err("Error in BatchWorker.forEachIndexed ["+index+"]:", e);
                } finally {
                    latch.countDown();
                }
            });
        }

        await(latch);
    }

    public <T> void forEach(List<Consumer<T>> items, T data) {
        if (items == null || items.isEmpty())
            return;

        CountDownLatch latch = new CountDownLatch(items.size());

        for (Consumer<T> item : items) {
            AsyncExecutor.register(() -> {
                try {
                    item.accept(data);
                } catch (Exception e) {
                    l.err("Error in BatchWorker.forEach: ", e);
                } finally {
                    latch.countDown();
                }
            });
        }

        await(latch);
    }

    private void await(CountDownLatch latch) {
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            l.err("BatchWorker interrupted while waiting", e);
        }
    }
}

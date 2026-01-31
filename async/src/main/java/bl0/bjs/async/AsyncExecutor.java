package bl0.bjs.async;

import bl0.bjs.common.async.control.IAsync;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class AsyncExecutor {
    private static final ConcurrentHashMap<IAsync, Future> data = new ConcurrentHashMap<>();
    private static final ExecutorService executor = Executors.newCachedThreadPool();

    /// <summary>
    /// register and start IAsync
    /// </summary>
    public static void register(IAsync task) {
        executor.execute(task);
    }

    /// <summary>
    /// stop and delete IAsync
    /// </summary>
    public static void unregister(IAsync task) {
        data.get(task).cancel(true);
        data.remove(task);
    }

    /// <summary>
    /// Stop und delete all tasks
    /// </summary>
    public static void stopAll() {
        data.forEach(((t, future) -> future.cancel(true)));
        data.clear();
    }
}

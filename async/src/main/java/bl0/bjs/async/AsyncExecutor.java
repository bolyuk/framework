package bl0.bjs.async;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class AsyncExecutor {
    private static final HashMap<IAsync, Future> data = new HashMap<>();
    private static final ExecutorService executor = Executors.newFixedThreadPool(10);

    /// <summary>
    /// register and start IAsync
    /// </summary>
    public static void register(IAsync task) {
        data.put(task,executor.submit(task));
    }

    /// <summary>
    /// stop and delete IAsync
    /// </summary>
    public static void unregister(IAsync task){
        data.get(task).cancel(true);
        data.remove(task);
    }
    /// <summary>
    /// Stop und delete all tasks
    /// </summary>
    public static void stopAll(){
        data.forEach(((t, future) -> future.cancel(true)));
        data.clear();
    }
}

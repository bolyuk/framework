package bl0.bjs.common.async.control;

import java.util.concurrent.Future;

public interface IAsyncController<T> {
    void interrupt(boolean alsoIfStarted);
    boolean isRunning();
    boolean isFaulted();
    boolean isCancelled();
    boolean isDone();
    Throwable getError();

    Future<T> getFuture();
}

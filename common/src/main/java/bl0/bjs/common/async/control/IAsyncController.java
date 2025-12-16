package bl0.bjs.common.async.control;

public interface IAsyncController {
    void interrupt();
    boolean isRunning();
    boolean isFaulted();
    Throwable getError();
}

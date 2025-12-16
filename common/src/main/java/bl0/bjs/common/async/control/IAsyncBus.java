package bl0.bjs.common.async.control;

public interface IAsyncBus {
    IAsyncController register(IAsync async);
    void unregister(IAsync async);
    void interruptAllThreads();
}

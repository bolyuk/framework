package bl0.bjs.async.control;

import bl0.bjs.common.async.control.IAsync;
import bl0.bjs.common.async.control.IAsyncBus;
import bl0.bjs.common.async.control.IAsyncController;
import bl0.bjs.common.base.BJSBaseClass;
import bl0.bjs.common.base.IContext;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

public class BaseThreadBus extends BJSBaseClass implements IAsyncBus {

    protected final ConcurrentHashMap<IAsync, WrappedFuture<?>> data = new ConcurrentHashMap<>();
    protected final ExecutorService executor;

    public BaseThreadBus(IContext ctx, ExecutorService executor) {
        super(ctx);
        this.executor = executor;
    }

    @Override
    public IAsyncController<?> register(IAsync async) {
        WrappedFuture<?> wf = new WrappedFuture<>(ctx, executor.submit(async));
        data.put(async, wf);
        return wf;
    }

    @Override
    public void unregister(IAsync async) {
        data.remove(async).interrupt(true);
    }

    @Override
    public void interruptAllThreads() {
        data.forEach((key, value) -> value.interrupt(true));
        data.clear();
    }
}

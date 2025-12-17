package bl0.bjs.async.control;

import bl0.bjs.common.async.control.IAsyncController;
import bl0.bjs.common.base.BJSBaseClass;
import bl0.bjs.common.base.IContext;
import bl0.bjs.common.core.event.Action;
import bl0.bjs.common.core.event.ActionController;

import java.util.concurrent.Future;

public class WrappedFuture<T> extends BJSBaseClass implements IAsyncController {
    protected final Future<T> future;
    public WrappedFuture(IContext ctx, Future<T> future) {
        super(ctx);
        this.future = future;
    }

    @Override
    public void interrupt(boolean alsoIfStarted) {
        future.cancel(alsoIfStarted);
    }

    @Override
    public boolean isRunning() {
        return future.state() == Future.State.RUNNING;
    }

    @Override
    public boolean isFaulted() {
        return future.state() == Future.State.FAILED;
    }

    @Override
    public boolean isDone() {
        return future.isDone();
    }

    @Override
    public boolean isCancelled() {
        return future.isCancelled();
    }

    @Override
    public Throwable getError() {
        return future.exceptionNow();
    }

    @Override
    public Future<T> getFuture() {
        return future;
    }
}

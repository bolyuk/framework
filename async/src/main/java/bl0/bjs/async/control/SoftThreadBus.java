package bl0.bjs.async.control;

import bl0.bjs.common.base.IContext;

import java.util.concurrent.Executors;

public class SoftThreadBus extends BaseThreadBus{
    public SoftThreadBus(IContext ctx) {
        super(ctx, Executors.newCachedThreadPool());
    }
}

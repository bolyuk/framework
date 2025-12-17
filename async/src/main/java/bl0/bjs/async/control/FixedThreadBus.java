package bl0.bjs.async.control;

import bl0.bjs.common.base.IContext;

import java.util.concurrent.Executors;

public class FixedThreadBus extends BaseThreadBus{
    public FixedThreadBus(IContext ctx, int threads) {
        super(ctx, Executors.newFixedThreadPool(threads));
    }
}

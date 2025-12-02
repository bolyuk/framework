package bl0.bjs.common.base;

import bl0.bjs.common.core.logging.ILogger;

public abstract class BJSBaseClass {
    protected final IContext ctx;
    protected final ILogger l;

    public BJSBaseClass(IContext ctx){
        throwIfNull(ctx, "Context");
        this.ctx = ctx;
        this.l = ctx.generateLogger(this.getClass());
    }

    protected void throwIfNull(Object value, String name) {
        if (value == null) {
            throw new NullPointerException("Value of " + name + " can't be null");
        } else if(value instanceof String s && s.isEmpty()) {
            throw new NullPointerException("Value of " + name + " can't be null");
        }
    }
}

package bl0.bjs.framework.logging;


import bl0.bjs.framework.Context;
import org.bl0.framework.common.base.eventbus.IEventBusController;
import org.bl0.framework.common.core.logging.ILogger;
import org.bl0.framework.common.core.logging.Level;
import org.bl0.framework.common.core.logging.containers.ILogBatch;
import org.bl0.framework.common.core.logging.containers.LogEntry;
import org.bl0.framework.common.sys.events.LogBatchEvent;
import org.bl0.framework.common.sys.events.LogEvent;

public class DefaultLogger implements ILogger {
    final IEventBusController<LogEvent, LogEvent.LogPayload> everyLogController;
    final IEventBusController<LogBatchEvent, LogBatchEvent.LogPayload> batchLogController;
    final Context context;
    final Class<?> binding;

    public DefaultLogger(Context context, Class<?> binding) {
        this.everyLogController = context.getEventBus().getController(LogEvent.class);
        this.batchLogController = context.getEventBus().getController(LogBatchEvent.class);
        this.context = context;
        this.binding = binding;
    }

    @Override
    public void log(Object msg, String extra_info) {
        add(new LogEntry(msg.toString(), extra_info, null, Level.INFO));
    }

    @Override
    public void log(Object msg) {
        add(new LogEntry(msg.toString(), null, null, Level.INFO));
    }

    @Override
    public void warn(Object msg, String extra_info) {
        add(new LogEntry(msg.toString(), extra_info, null, Level.WARNING));
    }

    @Override
    public void warn(Object msg) {
        add(new LogEntry(msg.toString(), null, null, Level.WARNING));
    }

    @Override
    public void err(Object msg) {
        add(new LogEntry(msg.toString(), null, null, Level.ERROR));
    }

    @Override
    public void err(Object msg, Throwable e) {
        add(new LogEntry(msg.toString(), null, e, Level.ERROR));
    }

    @Override
    public void err(Object msg, String extra_info) {
        add(new LogEntry(msg.toString(), extra_info, null, Level.ERROR));
    }

    @Override
    public void err(Object msg, String extra_info, Throwable e) {
        add(new LogEntry(msg.toString(), extra_info, e, Level.ERROR));
    }

    @Override
    public void add(LogEntry entry) {
        everyLogController.fireEvent(new LogEvent.LogPayload(binding, entry, null));
    }

    @Override
    public void flushBatch(ILogBatch batch) {
        batchLogController.fireEvent(new LogBatchEvent.LogPayload(binding, batch));
    }

    @Override
    public ILogBatch genBatch() {
        return new LogBatch(this);
    }

    @Override
    public ILogBatch genLocalBatchIfNull(ILogBatch batch) {
        if (batch == null)
            batch = new LogBatch(this, true);
        return batch;
    }
}

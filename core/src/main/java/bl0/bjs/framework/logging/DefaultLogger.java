package bl0.bjs.framework.logging;


import bl0.bjs.eventbus.IEventBusController;
import bl0.bjs.framework.Context;
import bl0.bjs.logging.ILogger;
import bl0.bjs.logging.Level;
import bl0.bjs.logging.containers.ILogBatch;
import bl0.bjs.logging.containers.LogEntry;
import bl0.bjs.logging.events.LogBatchEvent;
import bl0.bjs.logging.events.LogEvent;

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
        add(new LogEntry(msg.toString(), null, e.toString(), Level.ERROR));
    }

    @Override
    public void err(Object msg, String extra_info) {
        add(new LogEntry(msg.toString(), extra_info, null, Level.ERROR));
    }

    @Override
    public void err(Object msg, String extra_info, Throwable e) {
        add(new LogEntry(msg.toString(), extra_info, e.toString(), Level.ERROR));
    }

    @Override
    public void debug(Object msg, String extra_info) {
        add(new LogEntry(msg.toString(), extra_info, null, Level.DEBUG));
    }

    @Override
    public void debug(Object msg) {
        add(new LogEntry(msg.toString(), null, null, Level.DEBUG));
    }

    @Override
    public void debug(Object msg, Throwable e) {
        add(new LogEntry(msg.toString(), null, e.toString(), Level.DEBUG));
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

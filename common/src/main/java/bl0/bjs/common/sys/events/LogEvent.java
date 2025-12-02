package bl0.bjs.common.sys.events;


import bl0.bjs.common.base.eventbus.IEventBusNode;
import bl0.bjs.common.core.logging.containers.ILogBatch;
import bl0.bjs.common.core.logging.containers.LogEntry;

public interface LogEvent extends IEventBusNode<LogEvent.LogPayload> {
    public record LogPayload(Class<?> source, LogEntry entry, ILogBatch batch) {} }

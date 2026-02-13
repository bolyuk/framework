package bl0.bjs.logging.events;


import bl0.bjs.eventbus.IEventBusNode;
import bl0.bjs.logging.containers.ILogBatch;
import bl0.bjs.logging.containers.LogEntry;
import com.google.gson.annotations.Expose;

public interface LogEvent extends IEventBusNode<LogEvent.LogPayload> {
    record LogPayload(Class<?> source, LogEntry entry, ILogBatch batch) {
    }
}

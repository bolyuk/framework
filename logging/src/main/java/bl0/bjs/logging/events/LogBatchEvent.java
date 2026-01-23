package bl0.bjs.logging.events;

import bl0.bjs.eventbus.IEventBusNode;
import bl0.bjs.logging.containers.ILogBatch;

public interface LogBatchEvent extends IEventBusNode<LogBatchEvent.LogPayload> {
    record LogPayload(Class<?> source, ILogBatch batch) {
    }
}

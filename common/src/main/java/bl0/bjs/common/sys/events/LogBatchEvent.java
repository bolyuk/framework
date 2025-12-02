package bl0.bjs.common.sys.events;

import bl0.bjs.common.base.eventbus.IEventBusNode;
import bl0.bjs.common.core.logging.containers.ILogBatch;

public interface LogBatchEvent extends IEventBusNode<LogBatchEvent.LogPayload> { public record LogPayload(Class<?> source, ILogBatch batch) {} }

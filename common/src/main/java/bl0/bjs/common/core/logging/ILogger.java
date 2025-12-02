package bl0.bjs.common.core.logging;

import bl0.bjs.common.core.logging.containers.ILogBatch;
import bl0.bjs.common.core.logging.containers.LogEntry;

public interface ILogger {

    public void log(Object msg, String extra_info);
    public void log(Object msg);

    public void warn(Object msg, String extra_info);
    public void warn(Object msg);

    public void err(Object msg);
    public void err(Object msg, Throwable e);
    public void err(Object msg, String extra_info);
    public void err(Object msg, String extra_info, Throwable e);

    public void add(LogEntry entry);

    public ILogBatch genBatch();
    public ILogBatch genLocalBatchIfNull(ILogBatch batch);
    public void flushBatch(ILogBatch batch);
}

package bl0.bjs.logging;

import bl0.bjs.logging.containers.ILogBatch;
import bl0.bjs.logging.containers.LogEntry;

public interface ILogger {

    void log(Object msg, String extra_info);

    void log(Object msg);

    void warn(Object msg, String extra_info);

    void warn(Object msg);

    void err(Object msg);

    void err(Object msg, Throwable e);

    void err(Object msg, String extra_info);

    void err(Object msg, String extra_info, Throwable e);

    void debug(Object msg, String extra_info);
    void debug(Object msg);

    void debug(Object msg, Throwable e);

    void add(LogEntry entry);

    ILogBatch genBatch();

    ILogBatch genLocalBatchIfNull(ILogBatch batch);

    void flushBatch(ILogBatch batch);
}

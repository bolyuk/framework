package bl0.bjs.logging.containers;

public interface ILogBatch {
    void add(LogEntry log);

    void log(Object msg, String extra_info);

    void log(Object msg);

    void warn(Object msg, String extra_info);

    void warn(Object msg);

    void err(Object msg);

    void err(Object msg, Throwable e);

    void err(Object msg, String extra_info);

    void err(Object msg, String extra_info, Throwable e);

    void flush();

    void flushIfLocal();

    void flushAndClear();

    void clear();

    boolean isErrorPresent();

    boolean isLocalBatch();

    String getID();
}

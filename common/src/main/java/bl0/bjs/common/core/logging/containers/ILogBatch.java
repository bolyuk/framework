package bl0.bjs.common.core.logging.containers;

public interface ILogBatch {
    public void add(LogEntry log);
    public void log(Object msg, String extra_info);
    public void log(Object msg);

    public void warn(Object msg, String extra_info);
    public void warn(Object msg);

    public void err(Object msg);
    public void err(Object msg, Throwable e);
    public void err(Object msg, String extra_info);
    public void err(Object msg, String extra_info, Throwable e);

    public void flush();
    public void flushIfLocal();
    public void flushAndClear();
    public void clear();
    public boolean isErrorPresent();
    public boolean isLocalBatch();

    public String getID();
}

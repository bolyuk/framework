package bl0.bjs.framework.logging;


import bl0.bjs.logging.Level;
import bl0.bjs.logging.containers.ILogBatch;
import bl0.bjs.logging.containers.LogEntry;

import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

public final class LogBatch implements ILogBatch {
    public final ConcurrentLinkedQueue<LogEntry> logs = new ConcurrentLinkedQueue<>();
    private final DefaultLogger service;
    private final boolean isLocalBatch;
    private final UUID uuid = UUID.randomUUID();

    public LogBatch(DefaultLogger logger) {
        this.service = logger;
        isLocalBatch = false;
    }

    LogBatch(DefaultLogger logger, boolean isLocalBatch) {
        this.service = logger;
        this.isLocalBatch = isLocalBatch;
    }

    @Override
    public void add(LogEntry log) {
        logs.add(log);
        service.add(log);
    }

    @Override
    public void log(Object msg, String extra_info){
        add(new LogEntry(msg.toString(), extra_info, null, Level.INFO));
    }

    @Override
    public void log(Object msg) {
        add(new LogEntry(msg.toString(), null, null, Level.INFO));
    }

    @Override
    public void warn(Object msg, String extra_info){
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
    public void err(Object msg, String extra_info, Throwable e){
        add(new LogEntry(msg.toString(), extra_info, e, Level.ERROR));
    }

    @Override
    public void flush(){
        if(logs.isEmpty())
            return;
        this.service.flushBatch(this);
    }

    @Override
    public void flushIfLocal(){
        if(isLocalBatch)
            flush();
    }

    @Override
    public void flushAndClear(){
        flush();
        logs.clear();
    }

    @Override
    public void clear(){
        logs.clear();
    }

    @Override
    public boolean isErrorPresent(){
        return logs.stream().anyMatch(l -> l.level() == Level.ERROR || l.level() == Level.FATAL);
    }

    @Override
    public boolean isLocalBatch(){
        return isLocalBatch;
    }

    @Override
    public String getID(){return uuid.toString();}
}

package bl0.bjs.logging.utils;

import bl0.bjs.logging.containers.LogEntry;
import bl0.bjs.logging.events.LogEvent;

import java.time.temporal.ChronoUnit;

public class LogParser {

    /**
     * Gets a prefix for a log entry.
     * @param service a service name to be parsed
     * @return a prefix in [name] format or [?] if null
     */
    public static String getSimplePrefix(Class<?> service) {
        return service != null ? "[" + service.getSimpleName() + "]" : "[?]";
    }

    public static String parse(LogEvent.LogPayload data){
        String prefix = LogParser.getSimplePrefix(data.source());
        String msg = data.entry().message();

        //if extra msg is not too long, will be better to show it too...
        if (data.entry().extra_info() != null && data.entry().extra_info().lines().count() == 1)
            msg += " " + data.entry().extra_info();

        if (data.entry().exception() != null)
            msg += " " + data.entry().exception();

       return  "["+data.entry().time().truncatedTo(ChronoUnit.SECONDS)+ "]: " + prefix + " " + data.entry().level() + " - " +msg;
    }
}

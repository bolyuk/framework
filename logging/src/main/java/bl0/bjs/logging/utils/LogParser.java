package bl0.bjs.logging.utils;

import bl0.bjs.logging.ILogger;
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

    public static String getHierarchicalPrefix(ILogger logger) {
        StringBuilder builder = new StringBuilder();
        while (logger != null) {
            builder.append("[").append(logger.getBinding()).append("] ");
            logger = logger.getParentLogger();
        }
        return builder.toString();
    }

    public static String parse(LogEvent.LogPayload data){
        var parentLogger = data.entry().logger().getParentLogger();

        String prefix = parentLogger != null ?
                LogParser.getHierarchicalPrefix(parentLogger) : LogParser.getSimplePrefix(data.source());

        String msg = data.entry().message();

        if (data.entry().exception() != null)
            msg += " " + data.entry().exception();

        //if extra msg is not too long, will be better to show it too...
        if (data.entry().extra_info() != null && data.entry().extra_info().lines().count() == 1)
            msg += " " + data.entry().extra_info();

       return  "["+data.entry().time().truncatedTo(ChronoUnit.SECONDS)+ "]: " + prefix + " " + data.entry().level() + " - " +msg;
    }
}

package bl0.bjs.logging.utils;

public class LogParser {

    /**
     * Gets a prefix for a log entry.
     * @param service a service name to be parsed
     * @return a prefix in [name] format or [?] if null
     */
    public static String getSimplePrefix(Class<?> service){
        return service != null ? "["+service.getSimpleName()+"]":"[?]";
    }
}

package bl0.bjs.logging.containers;

import bl0.bjs.logging.Level;

/**
 * Log Entry Container class!
 */

public record LogEntry(String message, String extra_info, Throwable exception, Level level) { }

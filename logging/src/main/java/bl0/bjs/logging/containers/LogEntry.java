package bl0.bjs.logging.containers;

import bl0.bjs.logging.Level;
import com.google.gson.annotations.Expose;

/**
 * Log Entry Container class!
 */

public record LogEntry(@Expose String message,
                       @Expose String extra_info,
                       @Expose String exception,
                       @Expose Level level) {
}

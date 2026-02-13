package bl0.bjs.logging.containers;

import bl0.bjs.logging.Level;
import com.google.gson.annotations.Expose;

public record LogEntry(String message,String extra_info,String exception, Level level) {
}

package bl0.bjs.framework.core;

import bl0.bjs.framework.Context;
import bl0.bjs.common.base.IContext;
import bl0.bjs.common.core.logging.ILogger;
import bl0.bjs.common.core.logging.Level;
import bl0.bjs.common.core.logging.containers.LogEntry;
import bl0.bjs.common.sys.events.LogEvent;
import bl0.bjs.common.utils.LogParser;
import bl0.bjs.framework.files.LocalStorage;

public class BJSInitializer {
    private final Context ctx;
    private final ILogger logger;

    public BJSInitializer() {
        this.ctx = new Context(new LocalStorage(System.getProperty("user.dir")));
        this.logger = ctx.generateLogger(this.getClass());
    }

    public void startExceptionHandler(){
        Thread.setDefaultUncaughtExceptionHandler(this::catchExceptions);
    }

    public void showLogsInConsole(){
        ctx.getEventBus().getController(LogEvent.class).subscribe(this::log);
    }

    public IContext getContext(){
        return ctx;
    }

    public static void drawCoolLogo() {
        System.out.println("""               
                
                    ___  ________  ________  ___  ___  ___  ___  ________  _________  \s
                   |\\  \\|\\   __  \\|\\   ____\\|\\  \\|\\  \\|\\  \\|\\  \\|\\   __  \\|\\___   ___\\\s
                   \\ \\  \\ \\  \\|\\  \\ \\  \\___|\\ \\  \\\\\\  \\ \\  \\\\\\  \\ \\  \\|\\  \\|___ \\  \\_|\s
                 __ \\ \\  \\ \\  \\\\\\  \\ \\  \\  __\\ \\   __  \\ \\  \\\\\\  \\ \\   _  _\\   \\ \\  \\ \s
                |\\  \\\\_\\  \\ \\  \\\\\\  \\ \\  \\|\\  \\ \\  \\ \\  \\ \\  \\\\\\  \\ \\  \\\\  \\|   \\ \\  \\\s
                \\ \\________\\ \\_______\\ \\_______\\ \\__\\ \\__\\ \\_______\\ \\__\\\\ _\\    \\ \\__\\
                 \\|________|\\|_______|\\|_______|\\|__|\\|__|\\|_______|\\|__|\\|__|    \\|__|
                """);
    }

    private void log(LogEvent.LogPayload data) {
        String prefix = LogParser.getSimplePrefix(data.source());
        String msg = data.entry().message();

        //if extra msg is not too long, will be better to show it too...
        if (data.entry().extra_info() != null && data.entry().extra_info().lines().count() == 1)
            msg += " " + data.entry().extra_info();

        if (data.entry().exception() != null)
            msg += " "+data.entry().exception().getMessage();

        System.out.println(prefix + " " + data.entry().level() + " - " + msg);
    }

    private void catchExceptions(Thread t, Throwable e) {
        logger.add(new LogEntry("Uncaught exception!", null, e, Level.FATAL));
    }
}

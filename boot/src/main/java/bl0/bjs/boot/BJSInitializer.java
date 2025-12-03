package bl0.bjs.boot;

import bl0.bjs.framework.Context;
import bl0.bjs.common.base.IContext;
import bl0.bjs.logging.ILogger;
import bl0.bjs.logging.Level;
import bl0.bjs.logging.containers.LogEntry;
import bl0.bjs.logging.events.LogEvent;
import bl0.bjs.logging.utils.LogParser;
import bl0.bjs.framework.files.LocalStorage;

public class BJSInitializer {
    private final Context ctx;
    private final ILogger logger;

    public BJSInitializer(String hostname) {
        this.ctx = new Context(new LocalStorage(System.getProperty("user.dir")), hostname);
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

        System.out.println(ctx.getHostname().toUpperCase()+": "+prefix + " " + data.entry().level() + " - " + msg);
    }

    private void catchExceptions(Thread t, Throwable e) {
        logger.add(new LogEntry("Uncaught exception!", null, e, Level.FATAL));
    }

    public static IContext defaultInit(String hostname) {
        BJSInitializer.drawCoolLogo();

        BJSInitializer initializer = new BJSInitializer(hostname);
        initializer.showLogsInConsole();
        initializer.startExceptionHandler();
        return initializer.getContext();
    }
}

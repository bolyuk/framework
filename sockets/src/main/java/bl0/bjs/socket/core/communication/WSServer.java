package bl0.bjs.socket.core.communication;

import bl0.bjs.common.base.IContext;
import bl0.bjs.common.core.logging.ILogger;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;

public abstract class WSServer extends WebSocketServer {
    protected final ILogger l;
    protected final IContext ctx;

    public WSServer(IContext context, InetSocketAddress address) {
        super(address);
        this.ctx = context;
        this.l = ctx.generateLogger(this.getClass());
    }
}

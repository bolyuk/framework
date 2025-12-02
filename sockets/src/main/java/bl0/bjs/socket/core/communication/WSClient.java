package bl0.bjs.socket.core.communication;

import bl0.bjs.common.base.IContext;
import bl0.bjs.common.core.logging.ILogger;
import bl0.bjs.socket.base.IWSBase;
import bl0.bjs.socket.core.Socket;
import bl0.bjs.socket.core.SocketCallbackContainer;
import bl0.bjs.socket.services.IWebSocketService;
import bl0.bjs.socket.services.proxy.WSSAnswer;
import bl0.bjs.socket.services.proxy.WSSParcel;
import bl0.bjs.socket.services.proxy.WSSProxy;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.List;

import static bl0.bjs.socket.C.GSON;

public class WSClient extends WebSocketClient implements IWSBase {
    protected final ILogger l;
    protected final IContext ctx;

    private final SocketCallbackContainer callbackContainer;
    public WSClient(IContext context, URI serverUri) {
        super(serverUri);
        this.ctx = context;
        this.l = ctx.generateLogger(this.getClass());
        this.callbackContainer = new SocketCallbackContainer(context);
    }

    @Override
    public <T extends IWebSocketService> T get(Class<T> service) {
        return WSSProxy.bind(service, new Socket(getConnection(), callbackContainer), ctx);
    }

    @Override //TODO
    public <T extends IWebSocketService> T getNamed(Class<T> service, String name) {
        return null;
    }

    @Override //TODO
    public <T extends IWebSocketService> List<T> getAll(Class<T> service) {
        return List.of();
    }

    @Override
    public void onMessage(String s) {
        WSSAnswer parcel = GSON.fromJson(s, WSSAnswer.class);
        if(parcel != null)
            callbackContainer.feed(parcel);
        
    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        l.log("Connection opened");
    }

    @Override
    public void onClose(int i, String s, boolean b) {
        l.warn("Connection closed");
    }

    @Override
    public void onError(Exception e) {
        l.err(e);
    }
}

package bl0.bjs.socket.core.communication;

import bl0.bjs.common.base.IContext;
import bl0.bjs.logging.ILogger;
import bl0.bjs.socket.base.IWSBase;
import bl0.bjs.socket.core.WSSParcelRouter;
import bl0.bjs.socket.core.WSSResponseRouter;
import bl0.bjs.socket.services.IWebSocketService;
import bl0.bjs.socket.services.proxy.WSSParcel;
import bl0.bjs.socket.services.proxy.WSSProxy;
import bl0.bjs.socket.services.proxy.WSSResponse;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import static bl0.bjs.socket.C.GSON;

public class WSServer extends WebSocketServer implements IWSBase {
    protected final ILogger l;
    protected final IContext ctx;

    protected final WSSResponseRouter responseRouter;
    protected final WSSParcelRouter parcelRouter;

    protected final ArrayList<WebSocket> clients = new  ArrayList<>();

    private final Object lock = new Object();

    public WSServer(IContext context, InetSocketAddress address) {
        super(address);
        this.ctx = context;
        this.l = ctx.generateLogger(this.getClass());

        this.responseRouter = new WSSResponseRouter(context);
        this.parcelRouter = new WSSParcelRouter(context);
    }

    @Override // TODO more robust logic
    public <T extends IWebSocketService> T get(Class<T> service) {
        synchronized (lock) {
            return WSSProxy.bind(service, clients.getFirst(), ctx, responseRouter);
        }
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
    public void onOpen(WebSocket webSocket, ClientHandshake clientHandshake) {
        synchronized (lock) {
            clients.add(webSocket);
        }
    }

    @Override
    public void onClose(WebSocket webSocket, int i, String s, boolean b) {
        synchronized (lock) {
            clients.remove(webSocket);
        }
    }

    @Override // TODO need to delegate requests to another nodes if service named or not present here
    public void onMessage(WebSocket webSocket, String s) {
        JsonObject obj = JsonParser.parseString(s).getAsJsonObject();

        if (obj.has("data") && obj.has("type")) {
            WSSResponse answer = GSON.fromJson(obj, WSSResponse.class);
            responseRouter.pass(answer);
        } else if (obj.has("path") && obj.has("method")) {
            WSSParcel parcel = GSON.fromJson(obj, WSSParcel.class);
            parcelRouter.pass(parcel, webSocket);
        } else {
            l.warn("Unknown WS message: " + s);
        }
    }

    @Override
    public void onError(WebSocket webSocket, Exception e) {
        l.err(e);
    }

    @Override
    public void onStart() {
        l.log("Server started");
    }
}

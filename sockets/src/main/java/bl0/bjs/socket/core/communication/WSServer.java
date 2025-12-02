package bl0.bjs.socket.core.communication;

import bl0.bjs.common.base.IContext;
import bl0.bjs.logging.ILogger;
import bl0.bjs.socket.base.IWSBase;
import bl0.bjs.socket.core.data.WSSRegParcel;
import bl0.bjs.socket.services.proxy.WSSParcelRouter;
import bl0.bjs.socket.services.proxy.WSSResponseRouter;
import bl0.bjs.socket.core.NamedSocket;
import bl0.bjs.socket.services.IWebSocketService;
import bl0.bjs.socket.core.data.WSSParcel;
import bl0.bjs.socket.services.proxy.WSSProxy;
import bl0.bjs.socket.core.data.WSSResponse;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static bl0.bjs.socket.C.GSON;

public class WSServer extends WebSocketServer implements IWSBase {
    protected final ILogger l;
    protected final IContext ctx;

    protected final WSSResponseRouter responseRouter;
    protected final WSSParcelRouter parcelRouter;

    protected final ConcurrentHashMap<NamedSocket, List<String>> clients = new ConcurrentHashMap<>();

    public WSServer(IContext context, InetSocketAddress address) {
        super(address);
        this.ctx = context;
        this.l = ctx.generateLogger(this.getClass());

        this.responseRouter = new WSSResponseRouter(context);
        this.parcelRouter = new WSSParcelRouter(context);
    }

    @Override
    public <T extends IWebSocketService> T get(Class<T> service) {
        NamedSocket client = find(service.getName(), null);
        if (client == null) {
            l.warn(service.getName() + " not found");
            return null;
        }
        return WSSProxy.bind(service, client, ctx, responseRouter, null);
    }

    @Override //TODO
    public <T extends IWebSocketService> T getNamed(Class<T> service, String name) {
        NamedSocket client = find(service.getName(), name);
        if (client == null) {
            l.warn("Named service "+service.getName() + " not found");
            return null;
        }
        return WSSProxy.bind(service, client, ctx, responseRouter, null);
    }

    @Override //TODO
    public <T extends IWebSocketService> List<T> getAll(Class<T> service) {
        return List.of();
    }

    @Override
    public void onOpen(WebSocket webSocket, ClientHandshake clientHandshake) {
    }

    @Override
    public void onClose(WebSocket webSocket, int i, String s, boolean b) {
       NamedSocket socket = find(webSocket);
       if (socket != null) {
           clients.remove(socket);
           l.log(socket.getName() + " closed");
       }
    }

    @Override // TODO need to delegate requests to another nodes if service named or not present here
    public void onMessage(WebSocket webSocket, String s) {
        JsonObject obj = JsonParser.parseString(s).getAsJsonObject();
        NamedSocket socket = find(webSocket);

        if (obj.has("data") && obj.has("type")) {
            WSSResponse answer = GSON.fromJson(obj, WSSResponse.class);
            responseRouter.pass(answer);
        } else if (obj.has("path") && obj.has("method")) {
            WSSParcel parcel = GSON.fromJson(obj, WSSParcel.class);
            parcelRouter.pass(parcel, socket);
        } else if(obj.has("name") && obj.has("services")) {
            if(socket != null){
                l.warn(socket.getName()+" tried to authorize again...");
                return;
            }
            WSSRegParcel regParcel = GSON.fromJson(obj, WSSRegParcel.class);
            clients.put(new NamedSocket(ctx, webSocket, regParcel.getName()), List.of(regParcel.getServices()));
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

    private NamedSocket find(String clazz, String name) {
        if(clients.isEmpty())
            return null;

        for (var data : clients.entrySet()) {
            if(name != null && !data.getKey().getName().equals(name))
                continue;
            if(data.getValue() == null)
                continue;

            if(data.getValue().contains(clazz))
                return data.getKey();
        }
        return null;
    }

    private NamedSocket find(WebSocket socket) {
        if(clients.isEmpty())
            return null;
        for (var data : clients.keySet()) {
            if(data.getSocket().equals(socket) || data.getSocket().getRemoteSocketAddress().equals(socket.getRemoteSocketAddress()))
                return data;
        }
        return null;
    }
}

package bl0.bjs.socket.core.communication;

import bl0.bjs.common.base.IContext;
import bl0.bjs.logging.ILogger;
import bl0.bjs.socket.base.IWSBase;
import bl0.bjs.socket.core.WSSResponseRouter;
import bl0.bjs.socket.core.WSSParcelRouter;
import bl0.bjs.socket.services.IWebSocketService;
import bl0.bjs.socket.services.proxy.WSSResponse;
import bl0.bjs.socket.services.proxy.WSSParcel;
import bl0.bjs.socket.services.proxy.WSSProxy;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.List;

import static bl0.bjs.socket.C.GSON;

public class WSClient extends WebSocketClient implements IWSBase {
    protected final ILogger l;
    protected final IContext ctx;

    protected final WSSResponseRouter responseRouter;
    protected final WSSParcelRouter parcelRouter;

    public WSClient(IContext context, URI serverUri) {
        super(serverUri);
        this.ctx = context;
        this.l = ctx.generateLogger(this.getClass());

        this.responseRouter = new WSSResponseRouter(context);
        this.parcelRouter = new WSSParcelRouter(context);
    }

    @Override
    public <T extends IWebSocketService> T get(Class<T> service) {
        return WSSProxy.bind(service, getConnection(), ctx, responseRouter);
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
        JsonObject obj = JsonParser.parseString(s).getAsJsonObject();

        if (obj.has("data") && obj.has("type")) {
            WSSResponse answer = GSON.fromJson(obj, WSSResponse.class);
            responseRouter.pass(answer);
        } else if (obj.has("path") && obj.has("method")) {
            WSSParcel parcel = GSON.fromJson(obj, WSSParcel.class);
            parcelRouter.pass(parcel, getConnection());
        } else {
            l.warn("Unknown WS message: " + s);
        }
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

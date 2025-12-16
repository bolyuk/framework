package bl0.bjs.socket.core.communication;

import bl0.bjs.async.AsyncExecutor;
import bl0.bjs.async.queue.QueuePool;
import bl0.bjs.common.base.IContext;
import bl0.bjs.common.core.tuple.Pair;
import bl0.bjs.eventbus.IEventBusController;
import bl0.bjs.eventbus.IEventBusNode;
import bl0.bjs.logging.ILogger;
import bl0.bjs.socket.base.IWSBase;
import bl0.bjs.socket.core.ParcelQueue;
import bl0.bjs.socket.core.parcel.WSParcel;
import bl0.bjs.socket.core.parcel.payload.WSSEvent;
import bl0.bjs.socket.core.parcel.payload.auth.WSSAuth;
import bl0.bjs.socket.services.proxy.WSSResponseRouter;
import bl0.bjs.socket.services.proxy.WSSParcelRouter;
import bl0.bjs.socket.core.data.NamedSocket;
import bl0.bjs.socket.services.IWebSocketService;
import bl0.bjs.socket.services.proxy.WSSProxy;
import bl0.bjs.socket.utils.ParcelUtils;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.List;
import java.util.UUID;

public class WSClient extends WebSocketClient implements IWSBase {
    protected final ILogger l;
    protected final IContext ctx;

    protected final WSSResponseRouter responseRouter;
    protected final WSSParcelRouter parcelRouter;

    protected final String name;
    protected NamedSocket socket;

    protected final QueuePool<String, Pair<NamedSocket, WSParcel>, ParcelQueue> queuePool;

    public WSClient(IContext context, URI serverUri, String name) {
        super(serverUri);
        this.name = name;
        this.ctx = context;
        this.l = ctx.generateLogger(this.getClass());

        this.responseRouter = new WSSResponseRouter(context);
        this.parcelRouter = new WSSParcelRouter(context, name);


        this.queuePool = new QueuePool<>(ctx, (s) -> new ParcelQueue(parcelRouter, responseRouter, ctx, ParcelQueue::QueueWorker));
        this.queuePool.setMaxBatchSize(1);
    }

    @Override
    public <T extends IWebSocketService> T get(Class<T> service) {
        return WSSProxy.bind(service, new NamedSocket(ctx, getConnection(), null, false), ctx, responseRouter, this.name);
    }

    @Override
    public <T extends IWebSocketService> T getNamed(Class<T> service, String name) {
        return WSSProxy.bind(service, new NamedSocket(ctx, getConnection(), name, false), ctx, responseRouter, this.name);
    }

    @Override //TODO
    public <T extends IWebSocketService> List<T> getAll(Class<T> service) {
        return List.of();
    }

    @Override //TODO
    public <T extends IEventBusNode<T>> void connectEventBus(Class<T> dataClass) {
      IEventBusController<?,?> controller = ctx.getEventBus().getController(dataClass);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void onMessage(String s) {
        WSParcel bParcel = ParcelUtils.tryParse(s, socket, l, name);

        if(bParcel == null)
            return;

        if(socket == null){
            l.err("connection is dead ???");
            return;
        }

        queuePool.pass(bParcel.getFrom(), Pair.of(socket, bParcel));
    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        l.log("Connection opened");
        socket = new NamedSocket(ctx, getConnection(), NamedSocket.SERVER, false);
        authorize();
    }

    @Override
    public void onClose(int i, String s, boolean b) {
        socket = null;
        tryReconnect(10, s);
    }

    private void tryReconnect(int tries, String s) {
        if(tries == 0) {
            l.err("Reconnecting failed");
            return;
        }
        l.warn("Connection closed [ "+s+" ], reconnecting...");
        AsyncExecutor.register(()-> tryReconnect(tries-1, s));
    }

    private void authorize(){
        WSParcel parcel = genDefaultParcel(NamedSocket.SERVER);

        WSSAuth authPayload = new WSSAuth();
        authPayload.setName(name);
        parcel.setPayload(authPayload);

        socket.send(parcel);
        responseRouter.await(parcel.getUuid());
    }

    private WSParcel genDefaultParcel(String to){
        WSParcel parcel = new WSParcel();
        parcel.setFrom(name);
        parcel.setTo(to);
        parcel.setUuid(UUID.randomUUID());
        return parcel;
    }

    @Override
    public void onError(Exception e) {
        l.err(e);
    }

    private <T extends IEventBusNode<T>> void broadcast(T data) {
        WSParcel parcel = genDefaultParcel(NamedSocket.SERVER);
        parcel.setPayload(new WSSEvent(data));
        socket.send(parcel);
    }
}

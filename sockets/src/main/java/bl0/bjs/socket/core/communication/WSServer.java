package bl0.bjs.socket.core.communication;

import bl0.bjs.async.queue.BaseQueue;
import bl0.bjs.async.queue.QueuePool;
import bl0.bjs.common.base.IContext;
import bl0.bjs.common.core.tuple.Pair;
import bl0.bjs.eventbus.IEventBusNode;
import bl0.bjs.logging.ILogger;
import bl0.bjs.socket.base.IWSBase;
import bl0.bjs.socket.core.ParcelQueue;
import bl0.bjs.socket.core.parcel.WSParcel;
import bl0.bjs.socket.core.parcel.payload.WSSEvent;
import bl0.bjs.socket.core.parcel.payload.auth.WSSAuth;
import bl0.bjs.socket.services.proxy.WSSParcelRouter;
import bl0.bjs.socket.services.proxy.WSSResponseRouter;
import bl0.bjs.socket.core.data.NamedSocket;
import bl0.bjs.socket.services.IWebSocketService;
import bl0.bjs.socket.core.parcel.payload.WSSRequest;
import bl0.bjs.socket.services.proxy.WSSProxy;
import bl0.bjs.socket.utils.ParcelErrors;
import bl0.bjs.socket.utils.ParcelUtils;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class WSServer extends WebSocketServer implements IWSBase {
    protected final ILogger l;
    protected final IContext ctx;

    protected final WSSResponseRouter responseRouter;
    protected final WSSParcelRouter parcelRouter;

    protected final ConcurrentHashMap<NamedSocket, List<String>> clients = new ConcurrentHashMap<>();

    protected final BaseQueue<Pair<WebSocket, String>> acceptQueue;
    protected final QueuePool<String, Pair<NamedSocket, WSParcel>, ParcelQueue> queuePool;

    public WSServer(IContext context, InetSocketAddress address) {
        super(address);
        this.ctx = context;
        this.l = ctx.generateLogger(this.getClass());

        this.responseRouter = new WSSResponseRouter(context);
        this.parcelRouter = new WSSParcelRouter(context, NamedSocket.SERVER);

        this.queuePool = new QueuePool<>(ctx, (s) -> new ParcelQueue(parcelRouter, responseRouter, ctx, ParcelQueue::QueueWorker));
        this.queuePool.setMaxBatchSize(1);

        this.acceptQueue = new BaseQueue<>(ctx, this::acceptMessage);
        this.acceptQueue.setMaxBatchSize(1);
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

    @Override
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

    @Override //TODO
    public <T extends IEventBusNode<T>> void connectEventBus(Class<T> dataClass) {
    }

    @Override
    public String getName() {
        return NamedSocket.SERVER;
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

    @Override
    public void onMessage(WebSocket webSocket, String json) {
        acceptQueue.pass(List.of(Pair.of(webSocket, json)));
    }

    private void acceptMessage(BaseQueue<Pair<WebSocket, String>> stringQueue, List<Pair<WebSocket, String>> strings) {
        String json = strings.getFirst().second;
        WebSocket webSocket = strings.getFirst().first;

        NamedSocket client = find(webSocket);
        if(client == null)
            client = new NamedSocket(ctx, webSocket, null, false);

        WSParcel bParcel = ParcelUtils.tryParse(json, client, l, NamedSocket.SERVER);

        if(bParcel == null)
            return;

        if(!authGateway(bParcel, client, l))
            return;

        if(registerIfNeeded(client, bParcel))
            return;

        if(bParcel.getTo() != null && bParcel.getTo().equals(NamedSocket.SERVER)){
            queuePool.pass(bParcel.getFrom(), Pair.of(client, bParcel));
        } else {
            if(bParcel.getPayload() instanceof WSSEvent e){
                //TODO
            } else {
                String path = bParcel.getPayload() instanceof WSSRequest r ? r.getPath() : null;
                NamedSocket socket = find(path, bParcel.getTo());
                if (socket == null)
                    ParcelUtils.sendParcelErrorBackAndLog(ParcelErrors.RECIPIENT_NOT_FOUND, json, client, l, NamedSocket.SERVER);
                else {
                    socket.send(bParcel);
                    l.log("parcel rerouted from:" + bParcel.getFrom() + " to:" + socket.getName());
                }
            }
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

    protected NamedSocket find(String clazz, String name) {
        if(clients.isEmpty())
            return null;

        for (var data : clients.entrySet()) {
            if(name != null && !data.getKey().getName().equals(name))
                continue;

            if(clazz != null && data.getValue().contains(clazz))
                return data.getKey();

            if(clazz == null && data.getKey().getName().equals(name))
                return data.getKey();
        }
        return null;
    }

    protected List<NamedSocket> findAll(String clazz) {
        ArrayList<NamedSocket> result = new ArrayList<>();
        if(clients.isEmpty())
            return result;

        for (var data : clients.entrySet()) {
            if(clazz != null && data.getValue().contains(clazz))
                result.add(data.getKey());
        }
        return result;
    }

    private boolean authGateway(WSParcel parcel, NamedSocket client, ILogger l) {
        boolean isRegParcel = false;
        if(parcel.getPayload() instanceof WSSAuth authPayload)
            if(client.isAuthorized() || authPayload.getName() == null || parcel.getTo() == null || !parcel.getTo().equals(NamedSocket.SERVER)){
                ParcelUtils.sendParcelErrorBackAndLog(ParcelErrors.AUTH_WRONG_DATA, null, client, l, NamedSocket.SERVER);
                return false;
            } else
                isRegParcel = true;

        // unauthorized
        if(!client.isAuthorized() && !isRegParcel){
            ParcelUtils.sendParcelErrorBackAndLog(ParcelErrors.UNAUTHORIZED, null, client, l, NamedSocket.SERVER);
            return false;
        }
        return true;
    }

    private boolean registerIfNeeded(NamedSocket socket, WSParcel parcel) {
        if(parcel.getPayload() instanceof WSSAuth authPayload){
            socket = new NamedSocket(ctx, socket.getSocket(), authPayload.getName(), true);
            clients.put(socket, new ArrayList<>());
            l.log("client ["+socket.getName()+"] registered");
            return true;
        }
        return false;
    }

    protected NamedSocket find(WebSocket socket) {
        if(clients.isEmpty())
            return null;
        for (var data : clients.keySet()) {
            if(data.getSocket().equals(socket) || data.getSocket().getRemoteSocketAddress().equals(socket.getRemoteSocketAddress()))
                return data;
        }
        return null;
    }
}

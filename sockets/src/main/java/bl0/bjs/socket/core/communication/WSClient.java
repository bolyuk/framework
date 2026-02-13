package bl0.bjs.socket.core.communication;

import bl0.bjs.async.AsyncExecutor;
import bl0.bjs.async.queue.QueuePool;
import bl0.bjs.common.base.IContext;
import bl0.bjs.eventbus.IEventBusController;
import bl0.bjs.eventbus.IEventBusNode;
import bl0.bjs.logging.ILogger;
import bl0.bjs.services.IServiceExtender;
import bl0.bjs.services.Service;
import bl0.bjs.services.interfaces.IService;
import bl0.bjs.socket.base.IWSBase;
import bl0.bjs.socket.core.ParcelQueue;
import bl0.bjs.socket.core.data.NamedSocket;
import bl0.bjs.socket.core.parcel.WSParcel;
import bl0.bjs.socket.core.parcel.payload.WSSEvent;
import bl0.bjs.socket.core.parcel.payload.auth.WSSAuth;
import bl0.bjs.socket.services.IWebSocketService;
import bl0.bjs.socket.services.proxy.WSSParcelRouter;
import bl0.bjs.socket.services.proxy.WSSProxy;
import bl0.bjs.socket.services.proxy.WSSResponseRouter;
import bl0.bjs.socket.utils.ParcelUtils;
import com.google.gson.GsonBuilder;
import lombok.SneakyThrows;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class WSClient extends WebSocketClient implements IWSBase, IServiceExtender {
    protected final ILogger l;
    protected final IContext ctx;
    protected final WSSResponseRouter responseRouter;
    protected final WSSParcelRouter parcelRouter;

    protected final String name;

    protected final ArrayList<IEventBusController<?, ?>> connectedEventBusControllers = new ArrayList<>();

    protected NamedSocket socket;

    protected final QueuePool<String, ParcelQueue.QueueContainer, ParcelQueue> queuePool;

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

    @Override
    public <T extends IEventBusNode<R>, R> void connectEventBus(Class<T> dataClass) {
        IEventBusController<T, R> controller = ctx.getEventBus().getController(dataClass);
        controller.subscribeGeneric(this::broadcast);
        connectedEventBusControllers.add(controller);
    }


    @Override
    public String getName() {
        return name;
    }

    @Override
    public void onMessage(String s) {
        WSParcel bParcel = ParcelUtils.tryParse(s, socket, l, name);

        if (bParcel == null)
            return;

        if (socket == null) {
            l.err("connection is dead ???");
            return;
        }

        queuePool.pass(bParcel.getFrom(), new ParcelQueue.QueueContainer(socket, bParcel));
    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        l.debug("Connection opened");
        responseRouter.wsThread = Thread.currentThread();
        socket = new NamedSocket(ctx, getConnection(), NamedSocket.SERVER, false);
        authorize();
        ctx.getServiceContainer().addExtender(this);
        onConnected();
    }

    protected void onConnected() {
    }

    @Override
    public void onClose(int i, String s, boolean b) {
        socket = null;
        onDisconnected();
        tryReconnect(10, s);
    }

    private void onDisconnected() {
    }

    private void tryReconnect(int tries, String s) {
        if (tries == 0) {
            l.err("Reconnecting failed");
            return;
        }

        l.warn("Reconnecting... tries left=" + tries + " reason=" + s);

        AsyncExecutor.register(() -> {
            try {
                Thread.sleep(500);
                this.reconnectBlocking(); // или connectBlocking если клиент новый
            } catch (Exception e) {
                l.err("Reconnect failed", e);
                tryReconnect(tries - 1, s);
            }
        });
    }

    @SneakyThrows
    private void authorize() {
        WSParcel parcel = genDefaultParcel(NamedSocket.SERVER);

        WSSAuth authPayload = new WSSAuth();
        authPayload.setName(name);

        List<? extends Class<? extends IWebSocketService>> services = ctx.getServiceContainer()
                .getAllServices()
                .stream()
                .filter(IWebSocketService.class::isAssignableFrom).map(x -> (Class<? extends IWebSocketService>) x).toList();

        ArrayList<String> prepared = new ArrayList<>();

        for (Class<? extends IWebSocketService> service : services) {
            if (service.getAnnotation(Service.class) != null)
                for (var exp : service.getAnnotation(Service.class).exportServices())
                    prepared.add(exp.getSimpleName());
        }

        authPayload.setServices(prepared.toArray(String[]::new));
        parcel.setPayload(authPayload);

        responseRouter.prepare(parcel.getUuid());
        socket.send(parcel);
//        var response = responseRouter.await(parcel.getUuid());
//
//        if (response != null)
//            l.debug("Registration ACK");
//        else
//            l.debug("Registration FAILED");
    }

    private WSParcel genDefaultParcel(String to) {
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

    private <T extends IEventBusNode<R>, R> void broadcast(R data) {
        WSParcel parcel = genDefaultParcel(NamedSocket.SERVER);
        parcel.setPayload(new WSSEvent(data, new GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation()
                .create()));
        socket.send(parcel);
    }

    @Override
    public <T extends IService> T find(Class<T> serviceClass) {
        if (!IWebSocketService.class.isAssignableFrom(serviceClass))
            return null;

        return (T) get((Class<? extends IWebSocketService>) serviceClass);
    }
}

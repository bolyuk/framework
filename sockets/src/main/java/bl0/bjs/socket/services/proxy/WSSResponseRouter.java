package bl0.bjs.socket.services.proxy;

import bl0.bjs.common.async.stream.StreamChunk;
import bl0.bjs.common.base.BJSBaseClass;
import bl0.bjs.common.base.IContext;
import bl0.bjs.common.core.time.Timer;
import bl0.bjs.socket.base.IResponseAwaiter;
import bl0.bjs.socket.core.parcel.WSParcel;
import bl0.bjs.socket.core.parcel.payload.WSSResponse;
import bl0.bjs.socket.core.parcel.payload.WSStream;
import bl0.bjs.socket.services.proxy.stream.RemoteStreamProxy;
import com.google.gson.Gson;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class WSSResponseRouter extends BJSBaseClass implements IResponseAwaiter {

    private final Gson gson = new Gson();
    private final ConcurrentHashMap<UUID, AwaitState> awaiter = new ConcurrentHashMap<>();

    private final Object lock = new Object();

    public Thread wsThread;

    public WSSResponseRouter(IContext ctx) {
        super(ctx);
    }

    public void prepare(UUID uuid) {
        synchronized (lock) {
            AwaitState s = new AwaitState();
            s.isStream = false;
            awaiter.put(uuid, s);
        }
    }

    public void prepareStream(RemoteStreamProxy<?> sp) {
        synchronized (lock) {
            AwaitState s = new AwaitState();
            s.isStream = true;
            s.stream = sp;
            awaiter.put(sp.uuid, s);
        }
    }

    public Object await(UUID uuid) throws InterruptedException {
        throwIfWsThread(wsThread);
        AwaitState s = awaiter.get(uuid);
        if (s == null) throw new IllegalStateException("No awaiters for UUID " + uuid + " was prepared!");
        if (s.isStream) throw new IllegalStateException("Use awaitStream for stream " + uuid);
        if (s.result != null) {
            awaiter.remove(uuid);
            return s.result;
        }

        s.timer.start();
        boolean ok = s.latch.await(10, TimeUnit.SECONDS);
        awaiter.remove(uuid);
        if (!ok)
            l.debug("WSS request [" + uuid + "] failed");
        return ok ? s.result : null;
    }

    public void awaitStream(UUID uuid) throws InterruptedException {
        throwIfWsThread(wsThread);
        AwaitState s = awaiter.get(uuid);
        if (s == null) throw new IllegalStateException("No awaiters for UUID " + uuid + " was prepared!");
        if (!s.isStream) throw new IllegalStateException("not a stream " + uuid);
        s.timer.start();
        s.latch.await();
    }

    public boolean pass(WSParcel parcel) {
        if (!(parcel.getPayload() instanceof WSSResponse response)) {
            l.err("wrong payload [" + parcel.getPayloadType() + "] in ResponseRouter");
            return false;
        }
        AwaitState s = awaiter.get(parcel.getUuid());
        if (s == null) {
            return false;
        }

        if (response.getType() == null)
            return false;

        Object value;
        try {
            if (response.isSuccess())
                value = gson.fromJson(response.getData(), Class.forName(response.getType()));
            else
                value = new WSException(response.getData());
        } catch (ClassNotFoundException e) {
            l.err("response class [" + response.getType() + "] was not found");
            return false;
        }

        if (s.isStream) {
            return handleStream(s, response, value, parcel.getUuid());
        } else {
            l.debug("took: " + s.timer.stop() + "ms");
            s.result = value;
            s.latch.countDown();
        }
        return true;
    }

    private boolean handleStream(AwaitState s, WSSResponse response, Object value, UUID uuid) {
        if (!(response instanceof WSStream stream)) {
            s.stream.feedGeneric(new StreamChunk<>("Expected WSPseudoStream, got " + response.getClass().getSimpleName()));
            s.latch.countDown();
            return true;
        }

        if (stream.isACK) {
            l.debug("stream ack");
            s.latch.countDown();
            return true;
        }

        StreamChunk<?> res;
        if (response.isSuccess())
            res = new StreamChunk<>(stream.isDone, value);
        else
            res = new StreamChunk<>(response.getData());
        s.stream.feedGeneric(res);

        if (stream.isDone) {
            awaiter.remove(uuid);
            l.debug("stream done");
        }

        return true;
    }

    static final class AwaitState {
        final Timer timer = new Timer();
        final CountDownLatch latch = new CountDownLatch(1);
        volatile Object result;
        volatile RemoteStreamProxy<?> stream;
        volatile boolean isStream;
    }

    private static void throwIfWsThread(Thread wsThread) {
        if (wsThread == null)
            return; // ws thread ещё не зафиксирован — ничего не проверяем

        if (wsThread == Thread.currentThread()) {
            throw new IllegalStateException(
                    "Blocking operation is not permitted in WS thread [" +
                            wsThread.getName() + "]"
            );
        }
    }
}

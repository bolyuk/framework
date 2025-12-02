package bl0.bjs.socket.core;

import bl0.bjs.common.base.BJSBaseClass;
import bl0.bjs.common.base.IContext;
import bl0.bjs.common.core.tuple.Pair;
import bl0.bjs.socket.base.ISocketCallback;
import bl0.bjs.socket.services.proxy.WSSAnswer;
import bl0.bjs.socket.services.proxy.WSSParcel;
import com.google.gson.Gson;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class SocketCallbackContainer extends BJSBaseClass implements ISocketCallback {

    private final Gson gson = new Gson();
    private final ConcurrentHashMap<UUID, Pair<CountDownLatch, Object>> awaiter = new ConcurrentHashMap<>();

    public SocketCallbackContainer(IContext ctx) {
        super(ctx);
    }

    @Override
    public Object await(UUID uuid, Socket socket) {
        Pair<CountDownLatch, Object> pair =
                new Pair<>(new CountDownLatch(1), null);
        awaiter.put(uuid, pair);
        try {
            boolean ok = pair.first.await(10, TimeUnit.SECONDS);
            if (!ok) {
                l.err("Timeout for uuid "+uuid+" (10s)");
                return null;
            }
            return pair.second;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            l.err(e.getMessage());
            return null;
        } finally {
            awaiter.remove(uuid);
        }
    }

    public boolean feed(WSSAnswer parcel) {
        if(awaiter.containsKey(parcel.getUuid()))
        {
            Pair<CountDownLatch, Object> pair = awaiter.get(parcel.getUuid());
            Object value;
            if(parcel.getType() != null)
                try {
                    value = gson.fromJson(parcel.getData(), Class.forName(parcel.getType()));
                    pair.second = value;
                } catch (ClassNotFoundException e) {
                    pair.second = null;
                    l.err("response class " + parcel.getType()+" was not found");
                }
            pair.first.countDown();
            return true;
        }

        return false;
    }
}

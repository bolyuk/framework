package bl0.bjs.socket.services.proxy;

import bl0.bjs.common.base.BJSBaseClass;
import bl0.bjs.common.base.IContext;
import bl0.bjs.common.core.tuple.Pair;
import bl0.bjs.socket.base.IResponseAwaiter;
import bl0.bjs.socket.core.parcel.WSParcel;
import bl0.bjs.socket.core.parcel.payload.WSSResponse;
import com.google.gson.Gson;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class WSSResponseRouter extends BJSBaseClass implements IResponseAwaiter {

    private final Gson gson = new Gson();
    private final ConcurrentHashMap<UUID, Pair<CountDownLatch, Object>> awaiter = new ConcurrentHashMap<>();

    public WSSResponseRouter(IContext ctx) {
        super(ctx);
    }

    public void prepare(UUID uuid){
        Pair<CountDownLatch, Object> pair = Pair.of(new CountDownLatch(1), null);
        awaiter.put(uuid, pair);
    }

    public Object await(UUID uuid) {
        Pair<CountDownLatch, Object> pair = awaiter.getOrDefault(uuid, null);
        if(pair == null)
            throw new IllegalStateException("No awaiters for UUID " + uuid + " was prepared!");

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

    public boolean pass(WSParcel parcel) {
        if(parcel.getPayload() instanceof WSSResponse response) {
            Pair<CountDownLatch, Object> pair = awaiter.get(parcel.getUuid());
            if (pair == null) {
                return false;
            }

            Object value;
            if (response.getType() != null)
                try {
                    if (response.isSuccess())
                        value = gson.fromJson(response.getData(), Class.forName(response.getType()));
                    else
                        value = new WSException(response.getData());

                    pair.second = value;
                } catch (ClassNotFoundException e) {
                    pair.second = null;
                    l.err("response class [" + response.getType() + "] was not found");
                }
            pair.first.countDown();
            return true;
        } else {
            l.err("wrong payload ["+parcel.getPayloadType()+"] in WSSResponseRouter");
            return  false;
        }
    }
}

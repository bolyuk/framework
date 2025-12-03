package bl0.bjs.socket.services.proxy;

import bl0.bjs.common.base.BJSBaseClass;
import bl0.bjs.common.base.IContext;
import bl0.bjs.common.core.tuple.Pair;
import bl0.bjs.socket.base.IResponseAwaiter;
import bl0.bjs.socket.core.data.WSException;
import bl0.bjs.socket.core.data.WSSResponse;
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

    public Object await(UUID uuid) {
        Pair<CountDownLatch, Object> pair = Pair.of(new CountDownLatch(1), null);
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

    public boolean pass(WSSResponse parcel) {
        Pair<CountDownLatch, Object> pair = awaiter.get(parcel.getUuid());
        if (pair == null) {
            return false;
        }

        Object value;
        if(parcel.getParcelType() != null)
            try {
                if(parcel.isSuccess())
                    value = gson.fromJson(parcel.getData(), Class.forName(parcel.getParcelType()));
                else
                    value = new WSException(parcel.getData());

                pair.second = value;
            } catch (ClassNotFoundException e) {
                pair.second = null;
                l.err("response class " + parcel.getParcelType()+" was not found");
            }
        pair.first.countDown();
        return true;
    }
}

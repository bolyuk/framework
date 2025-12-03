package bl0.bjs.socket.core;

import bl0.bjs.async.queue.Queue;
import bl0.bjs.common.base.IContext;
import bl0.bjs.common.core.tuple.Pair;
import bl0.bjs.socket.core.data.NamedSocket;
import bl0.bjs.socket.core.data.WSParcel;
import bl0.bjs.socket.core.payload.WSSRequest;
import bl0.bjs.socket.core.payload.WSSResponse;
import bl0.bjs.socket.services.proxy.WSSParcelRouter;
import bl0.bjs.socket.services.proxy.WSSResponseRouter;

import java.util.List;
import java.util.function.BiConsumer;

public class ParcelQueue extends Queue<Pair<NamedSocket, WSParcel>> {
    public final WSSParcelRouter parcelRouter;
    public final WSSResponseRouter responseRouter;
    public ParcelQueue(WSSParcelRouter parcelRouter, WSSResponseRouter responseRouter, IContext ctx, BiConsumer<Queue<Pair<NamedSocket, WSParcel>>, List<Pair<NamedSocket, WSParcel>>> queueFunction) {
        super(ctx, queueFunction);
        this.parcelRouter = parcelRouter;
        this.responseRouter = responseRouter;
    }

    public static void QueueWorker(Queue<Pair<NamedSocket, WSParcel>> q, List<Pair<NamedSocket, WSParcel>> data){
        ParcelQueue queue = (ParcelQueue) q;
        NamedSocket socket = data.getFirst().first;
        WSParcel parcel = data.getFirst().second;

        if(parcel.getPayload().getClass().equals(WSSRequest.class))
                queue.parcelRouter.feed(parcel, socket);
        else if(parcel.getPayload().getClass().equals(WSSResponse.class))
                queue.responseRouter.pass(parcel);
    }
}

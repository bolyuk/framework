package bl0.bjs.socket.core;

import bl0.bjs.async.queue.Queue;
import bl0.bjs.common.base.IContext;
import bl0.bjs.common.core.tuple.Pair;
import bl0.bjs.socket.core.data.WSBaseParcel;
import bl0.bjs.socket.core.data.WSSParcel;
import bl0.bjs.socket.core.data.WSSResponse;
import bl0.bjs.socket.services.proxy.WSSParcelRouter;
import bl0.bjs.socket.services.proxy.WSSResponseRouter;

import java.util.List;
import java.util.function.BiConsumer;

public class ParcelQueue extends Queue<Pair<NamedSocket, WSBaseParcel>> {
    public final WSSParcelRouter parcelRouter;
    public final WSSResponseRouter responseRouter;
    public ParcelQueue(WSSParcelRouter parcelRouter, WSSResponseRouter responseRouter, IContext ctx, BiConsumer<Queue<Pair<NamedSocket, WSBaseParcel>>, List<Pair<NamedSocket, WSBaseParcel>>> queueFunction) {
        super(ctx, queueFunction);
        this.parcelRouter = parcelRouter;
        this.responseRouter = responseRouter;
    }

    public static void QueueWorker(Queue<Pair<NamedSocket, WSBaseParcel>> q, List<Pair<NamedSocket, WSBaseParcel>> data){
        ParcelQueue queue = (ParcelQueue) q;
        NamedSocket socket = data.getFirst().first;
        WSBaseParcel parcel = data.getFirst().second;

        if(parcel.getClass().equals(WSSParcel.class))
                queue.parcelRouter.feed((WSSParcel)parcel, socket);
        else if(parcel.getClass().equals(WSSResponse.class))
                queue.responseRouter.pass((WSSResponse)parcel);
    }
}

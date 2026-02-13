package bl0.bjs.socket.services.proxy.stream;

import bl0.bjs.common.async.stream.IStream;
import bl0.bjs.common.async.stream.StreamChunk;
import bl0.bjs.common.core.event.action.Action;
import bl0.bjs.common.core.tuple.Pair;
import bl0.bjs.socket.base.IResponseAwaiter;
import bl0.bjs.socket.core.data.NamedSocket;
import bl0.bjs.socket.core.parcel.WSParcel;
import lombok.Setter;
import lombok.SneakyThrows;

import java.util.UUID;
import java.util.function.Function;

public class RemoteStreamProxy<T> implements IStream<T> {
    private T data;
    public final UUID uuid;
    private final NamedSocket socket;
    private final IResponseAwaiter awaiter;
    private final WSParcel parcel;


    @Setter
    private Function<Pair<StreamChunk<T>, T>, T> accumulator;

    @Setter
    private Action<T> deltaListener;

    public RemoteStreamProxy(NamedSocket socket, UUID uuid, IResponseAwaiter awaiter, WSParcel parcel) {
        this.socket = socket;
        this.uuid = uuid;
        this.awaiter = awaiter;
        this.parcel = parcel;
    }

    @Override
    public void feed(StreamChunk<T> data) {
        if(accumulator != null)
            this.data = accumulator.apply(Pair.of(data, this.data));

        if(deltaListener != null)
            deltaListener.invoke(data.data);
    }

    public void feedGeneric(StreamChunk<?> data) {
        feed((StreamChunk<T>) data);
    }

    @Override //TODO
    public void cancel() {
        throw  new UnsupportedOperationException("Not supported yet.");
    }

    @SneakyThrows
    @Override
    public void start() {
        socket.send(parcel);
        awaiter.awaitStream(this.uuid);
    }
}

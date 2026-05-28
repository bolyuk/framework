package bl0.bjs.socket.services.proxy.stream;

import bl0.bjs.common.async.stream.DefaultStream;
import bl0.bjs.socket.base.IResponseAwaiter;
import bl0.bjs.socket.core.data.NamedSocket;
import bl0.bjs.socket.core.parcel.WSParcel;
import lombok.SneakyThrows;

import java.util.UUID;

public class RemoteStreamProxy<T> extends DefaultStream<T> {
    public final UUID uuid;
    private final NamedSocket socket;
    private final IResponseAwaiter awaiter;
    private final WSParcel parcel;

    public RemoteStreamProxy(NamedSocket socket, UUID uuid, IResponseAwaiter awaiter, WSParcel parcel) {
        super(null);
        this.socket = socket;
        this.uuid = uuid;
        this.awaiter = awaiter;
        this.parcel = parcel;
    }

    @Override
    public void cancel() {
        throw new UnsupportedOperationException("Not supported.");
    }

    @SneakyThrows
    @Override
    public void start() {
        socket.send(parcel);
        awaiter.awaitStream(this.uuid);
    }
}

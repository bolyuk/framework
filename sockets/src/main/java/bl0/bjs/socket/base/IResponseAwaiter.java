package bl0.bjs.socket.base;

import bl0.bjs.socket.services.proxy.StreamProxy;

import java.util.UUID;

public interface IResponseAwaiter {
    Object await(UUID uuid) throws InterruptedException;

    void prepare(UUID uuid);

    void prepareStream(StreamProxy<?> streamProxy);

    void awaitStream(UUID uuid) throws InterruptedException;
}

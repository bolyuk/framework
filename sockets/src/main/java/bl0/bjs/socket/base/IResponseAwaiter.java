package bl0.bjs.socket.base;

import java.util.UUID;

public interface IResponseAwaiter {
    Object await(UUID uuid);

    void prepare(UUID uuid);
}

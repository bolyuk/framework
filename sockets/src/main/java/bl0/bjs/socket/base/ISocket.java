package bl0.bjs.socket.base;

import java.util.UUID;

public interface ISocket {
    boolean isClosed();
    Object sendAndWait(String msg, UUID uuid);
    String getAddress();

    void send(String json);
}

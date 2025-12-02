package bl0.bjs.socket.base;

import bl0.bjs.socket.core.Socket;

import java.util.UUID;

public interface ISocketCallback {
    Object await(UUID uuid, Socket socket);
}

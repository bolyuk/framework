package bl0.bjs.socket.core;

import bl0.bjs.socket.base.ISocket;
import bl0.bjs.socket.base.ISocketCallback;
import org.java_websocket.WebSocket;

import java.util.UUID;

public class Socket implements ISocket {
    private final WebSocket socket;
    private final ISocketCallback callBack;
    public Socket(WebSocket socket, ISocketCallback callBack) {
        this.socket = socket;
        this.callBack = callBack;
    }

    public boolean isClosed() {
        return socket.isClosed();
    }

    public Object sendAndWait(String msg, UUID uuid){
        socket.send(msg);
       return callBack.await(uuid, this);
    }

    public String getAddress(){
        return socket.getRemoteSocketAddress().toString();
    }

    @Override
    public void send(String json) {
        socket.send(json);
    }
}

package bl0.bjs.socket.core;

import bl0.bjs.common.base.BJSBaseClass;
import bl0.bjs.common.base.IContext;
import lombok.Getter;
import org.java_websocket.WebSocket;

@Getter
public class NamedSocket extends BJSBaseClass {
    private final WebSocket socket;
    private final String name;

    public NamedSocket(IContext ctx, WebSocket socket, String name) {
        super(ctx);
        this.socket = socket;
        this.name = name;
    }

    public String getAddress(){
        return socket.getRemoteSocketAddress().toString();
    }

    public void send(String data){
        socket.send(data);
    }

    public boolean isClosed(){
        return socket.isClosed();
    }
}

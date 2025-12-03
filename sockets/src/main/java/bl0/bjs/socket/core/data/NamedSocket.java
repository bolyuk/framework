package bl0.bjs.socket.core.data;

import bl0.bjs.common.base.BJSBaseClass;
import bl0.bjs.common.base.IContext;
import lombok.Getter;
import org.java_websocket.WebSocket;

import static bl0.bjs.socket.C.GSON;

@Getter
public class NamedSocket extends BJSBaseClass {
    private final WebSocket socket;
    private final String name;

    private final boolean isAuthorized;

    public NamedSocket(IContext ctx, WebSocket socket, String name, boolean authorized) {
        super(ctx);
        this.socket = socket;
        this.name = name;
        this.isAuthorized = authorized;
    }

    public String getAddress(){
        return socket.getRemoteSocketAddress().toString();
    }

    public void send(WSParcel data){
        data.beforeSend();
        socket.send(GSON.toJson(data));
    }

    public boolean isClosed(){
        return socket.isClosed();
    }

    public static final String SERVER = "SERVER";
}

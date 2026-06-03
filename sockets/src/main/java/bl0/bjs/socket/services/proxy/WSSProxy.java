package bl0.bjs.socket.services.proxy;

import bl0.bjs.common.async.stream.IStream;
import bl0.bjs.common.base.IContext;
import bl0.bjs.common.core.tuple.Pair;
import bl0.bjs.logging.ILogger;
import bl0.bjs.socket.base.IResponseAwaiter;
import bl0.bjs.socket.core.data.NamedSocket;
import bl0.bjs.socket.core.parcel.WSParcel;
import bl0.bjs.socket.core.parcel.payload.WSSRequest;
import bl0.bjs.socket.services.IWebSocketService;
import bl0.bjs.socket.services.proxy.stream.RemoteStreamProxy;
import lombok.SneakyThrows;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.*;
import java.util.UUID;

import static bl0.bjs.socket.C.GSON;

public class WSSProxy {
    @SuppressWarnings("unchecked")
    public static <T extends IWebSocketService> Pair<T, WSInvocationHandler> bind(Class<T> iface, NamedSocket socket, IContext ctx, IResponseAwaiter waiter, String name) {
        ILogger l = ctx.generateLogger(iface);
        WSInvocationHandler invHandler = new WSInvocationHandler(socket, waiter, l, name, iface);
        return Pair.of ((T) Proxy.newProxyInstance(
                iface.getClassLoader(),
                new Class<?>[]{iface}, invHandler), invHandler);
    }


}

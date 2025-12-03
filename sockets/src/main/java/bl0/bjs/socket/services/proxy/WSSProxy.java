package bl0.bjs.socket.services.proxy;

import bl0.bjs.common.base.IContext;
import bl0.bjs.logging.ILogger;
import bl0.bjs.socket.base.IResponseAwaiter;
import bl0.bjs.socket.core.NamedSocket;
import bl0.bjs.socket.core.data.WSSParcel;
import bl0.bjs.socket.services.IWebSocketService;
import lombok.SneakyThrows;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;

import static bl0.bjs.socket.C.GSON;

public class WSSProxy {
    @SuppressWarnings("unchecked")
    public static <T extends IWebSocketService> T bind(Class<T> iface, NamedSocket socket, IContext ctx, IResponseAwaiter waiter, String name) {
        ILogger l = ctx.generateLogger(iface);
        return (T) Proxy.newProxyInstance(
                iface.getClassLoader(),
                new Class<?>[]{iface},
                (proxy, method, args) -> proxyMethod(method, args, socket, iface, l, waiter, name));
    }

    @SneakyThrows
    private static Object proxyMethod(Method method, Object[] args, NamedSocket socket, Class<?> iface, ILogger l, IResponseAwaiter waiter, String name) {
        if(socket == null || socket.isClosed())
            throw new IllegalStateException("Socket is closed!");

        UUID uuid = UUID.randomUUID();
        WSSParcel parcel = new WSSParcel();

        parcel.setMethod(method.getName());
        parcel.setUuid(uuid);
        parcel.setFrom(name);
        parcel.setTo(socket.getName());
        parcel.setPath(iface.getName());

        int len = args == null ? 0 : args.length;
        String[] paramTypes = new String[len];
        String[] params = new String[len];

        for (int i = 0; i < len; i++) {
            params[i] = GSON.toJson(args[i]);
            paramTypes[i] = method.getParameterTypes()[i].getName();
        }

        parcel.setParams(params);
        parcel.setParamTypes(paramTypes);

        l.log(iface.getSimpleName()+"."+method.getName()+" ip: "+socket.getAddress());

        socket.send(GSON.toJson(parcel));
        if (method.getReturnType() == Void.TYPE) {
            return null;
        } else {
            Object data = waiter.await(parcel.getUuid());
            if(data instanceof Throwable t)
                throw t;
            return data;
        }
    }
}

package bl0.bjs.socket.services.proxy;

import bl0.bjs.common.base.IContext;
import bl0.bjs.common.core.logging.ILogger;
import bl0.bjs.socket.base.ISocket;
import bl0.bjs.socket.services.IWebSocketService;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;

import static bl0.bjs.socket.C.GSON;

public class WSSProxy {
    @SuppressWarnings("unchecked")
    public static <T extends IWebSocketService> T bind(Class<T> iface, ISocket socket, IContext ctx) {
        ILogger l = ctx.generateLogger(iface);
        return (T) Proxy.newProxyInstance(
                iface.getClassLoader(),
                new Class<?>[]{iface},
                (proxy, method, args) -> proxyMethod(method, args, socket, iface, l));
    }

    private static Object proxyMethod(Method method, Object[] args, ISocket socket, Class<?> iface, ILogger l) {
        if(socket == null || socket.isClosed())
            throw new IllegalStateException("Socket is closed!");

        UUID uuid = UUID.randomUUID();
        WSSParcel parcel = new WSSParcel();

        parcel.setMethod(method.getName());
        parcel.setUuid(uuid);
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

        if (method.getReturnType() == Void.TYPE) {
            socket.send(GSON.toJson(parcel));
            return null;
        } else {
            return socket.sendAndWait(GSON.toJson(parcel), uuid);
        }
    }
}

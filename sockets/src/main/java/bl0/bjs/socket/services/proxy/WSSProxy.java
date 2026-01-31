package bl0.bjs.socket.services.proxy;

import bl0.bjs.async.stream.IStream;
import bl0.bjs.common.base.IContext;
import bl0.bjs.logging.ILogger;
import bl0.bjs.socket.base.IResponseAwaiter;
import bl0.bjs.socket.core.data.NamedSocket;
import bl0.bjs.socket.core.parcel.WSParcel;
import bl0.bjs.socket.core.parcel.payload.WSSRequest;
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
        if (socket == null || socket.isClosed())
            throw new IllegalStateException("Socket is closed!");

        UUID uuid = UUID.randomUUID();
        WSParcel parcel = new WSParcel();


        parcel.setUuid(uuid);
        parcel.setFrom(name);
        parcel.setTo(socket.getName());

        WSSRequest request = new WSSRequest();
        parcel.setPayload(request);

        request.setPath(iface.getName());
        request.setMethod(method.getName());

        int len = args == null ? 0 : args.length;
        String[] paramTypes = new String[len];
        String[] params = new String[len];

        for (int i = 0; i < len; i++) {
            params[i] = GSON.toJson(args[i]);
            paramTypes[i] = method.getParameterTypes()[i].getName();
        }

        request.setParams(params);
        request.setParamTypes(paramTypes);

        l.log(iface.getSimpleName() + "." + method.getName() + " ip: " + socket.getAddress());

        if (method.getReturnType() == Void.TYPE) {
            socket.send(parcel);
            return null;
        } else if (IStream.class == method.getReturnType()) {
            var StreamProxy = new StreamProxy<>(socket, parcel.getUuid(), waiter, parcel);
            waiter.prepareStream(StreamProxy);
            return StreamProxy;
        } else {
            waiter.prepare(parcel.getUuid());
            socket.send(parcel);
            Object data = waiter.await(parcel.getUuid());
            if (data instanceof Throwable t)
                throw t;
            return data;
        }
    }
}

package bl0.bjs.socket.services.proxy;

import bl0.bjs.common.async.stream.IStream;
import bl0.bjs.logging.ILogger;
import bl0.bjs.socket.base.IResponseAwaiter;
import bl0.bjs.socket.core.data.NamedSocket;
import bl0.bjs.socket.core.parcel.WSParcel;
import bl0.bjs.socket.core.parcel.payload.WSSRequest;
import bl0.bjs.socket.services.proxy.stream.RemoteStreamProxy;
import lombok.SneakyThrows;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.*;
import java.util.UUID;

import static bl0.bjs.socket.C.GSON;

public class WSInvocationHandler implements InvocationHandler {

    private final String name;

    private final IResponseAwaiter waiter;
    private final ILogger l;
    private final Class<?> iface;

    private NamedSocket socket;

    public WSInvocationHandler(NamedSocket socket, IResponseAwaiter waiter, ILogger l, String name, Class<?> iface) {
        this.name = name;
        this.waiter = waiter;
        this.l = l;
        this.socket = socket;
        this.iface = iface;
    }

    public void rebindSocket(NamedSocket socket) {
        this.socket = socket;
    }

    @SneakyThrows
    private Object proxyMethod(Method method, Object[] args) {
        if (method.getName().equals("toString")) {
            return "WSSProxy [" + socket.getName() + "]";
        }

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
            paramTypes[i] = resolveParamType(method, i, iface);
        }

        request.setParams(params);
        request.setParamTypes(paramTypes);

        l.log(iface.getSimpleName() + "." + method.getName() + " ip: " + socket.getAddress());

        if (method.getReturnType() == Void.TYPE) {
            socket.send(parcel);
            return null;
        } else if (IStream.class == method.getReturnType()) {
            var StreamProxy = new RemoteStreamProxy<>(socket, parcel.getUuid(), waiter, parcel);
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

    private static String resolveParamType(Method method, int index, Class<?> iface) {
        Type paramTypeGeneric = method.getGenericParameterTypes()[index];

        if (paramTypeGeneric instanceof TypeVariable<?> tv) {
            Type resolved = resolveTypeVariable(tv, iface);
            return resolved != null ? normalizeTypeName(resolved) : classToName(method.getParameterTypes()[index]);
        }

        if (paramTypeGeneric instanceof ParameterizedType pt) {
            return resolveParameterizedType(pt, iface, null);
        }

        return classToName(method.getParameterTypes()[index]);
    }

    private static String resolveParameterizedType(ParameterizedType pt, Class<?> iface, @Nullable Class<?> rawOverride) {
        Class<?> raw = rawOverride != null ? rawOverride : (Class<?>) pt.getRawType();
        StringBuilder sb = new StringBuilder();
        sb.append(classToName(raw)).append("<");

        Type[] args = pt.getActualTypeArguments();
        for (int i = 0; i < args.length; i++) {
            if (i > 0) sb.append(", ");
            Type arg = args[i];

            if (arg instanceof TypeVariable<?> tv) {
                Type resolved = resolveTypeVariable(tv, iface);
                sb.append(resolved != null ? normalizeTypeName(resolved) : tv.getName());
            } else if (arg instanceof ParameterizedType nested) {
                sb.append(resolveParameterizedType(nested, iface, null));
            } else {
                sb.append(normalizeTypeName(arg));
            }
        }

        return sb.append(">").toString();
    }

    private static String normalizeTypeName(Type type) {
        return type.getTypeName().replace('$', '.');
    }

    private static String classToName(Class<?> clazz) {
        return clazz.getName().replace('$', '.');
    }

    private static Type resolveTypeVariable(TypeVariable<?> tv, Class<?> iface) {
        for (Type superIface : iface.getGenericInterfaces()) {
            if (!(superIface instanceof ParameterizedType pt)) continue;

            Class<?> rawType = (Class<?>) pt.getRawType();
            TypeVariable<?>[] typeParams = rawType.getTypeParameters();
            Type[] typeArgs = pt.getActualTypeArguments();

            for (int i = 0; i < typeParams.length; i++) {
                if (typeParams[i].getName().equals(tv.getName())) {
                    return typeArgs[i];
                }
            }
        }
        return null;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        return proxyMethod(method, args);
    }
}

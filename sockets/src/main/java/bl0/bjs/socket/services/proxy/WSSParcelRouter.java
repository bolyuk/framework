package bl0.bjs.socket.services.proxy;

import bl0.bjs.async.AsyncExecutor;
import bl0.bjs.common.async.stream.IStream;
import bl0.bjs.common.base.BJSBaseClass;
import bl0.bjs.common.base.IContext;
import bl0.bjs.socket.core.data.NamedSocket;
import bl0.bjs.socket.core.parcel.WSParcel;
import bl0.bjs.socket.core.parcel.payload.WSStream;
import bl0.bjs.socket.core.parcel.payload.WSSRequest;
import bl0.bjs.socket.core.parcel.payload.WSSResponse;
import bl0.bjs.socket.services.IWebSocketService;
import com.google.gson.reflect.TypeToken;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static bl0.bjs.socket.C.GSON;

public class WSSParcelRouter extends BJSBaseClass {

    private final ConcurrentHashMap<UUID, IStream<?>> boundStreams = new ConcurrentHashMap<>();
    private final String name;

    public WSSParcelRouter(IContext ctx, String name) {
        super(ctx);
        this.name = name;
    }

    public void feed(WSParcel parcel, NamedSocket socket) {
        if (parcel.getPayload() instanceof WSSRequest request) {

            WSParcel answerParcel = new WSParcel();
            WSSResponse answerPayload = new WSSResponse();
            answerParcel.setPayload(answerPayload);
            answerParcel.setTo(parcel.getFrom());
            answerParcel.setFrom(name);
            answerParcel.setUuid(parcel.getUuid());

            try {
                Class<?> clazz = Class.forName(request.getPath());

                if (!IWebSocketService.class.isAssignableFrom(clazz))
                    throw new ClassCastException(request.getPath() + " is not assignable to IWebSocketService");

                Object service = ctx.getServiceContainer().get((Class<? extends IWebSocketService>) clazz);

                if (service == null)
                    throw new NullPointerException(request.getPath() + " does not exist");


                ResolvedParams resolvedParams = resolveParamTypes(request.getParamTypes());
                Object[] params = resolveParams(request.getParams(), resolvedParams.gsonTypes());

                Method method = resolveMethod(service.getClass(), request.getMethod(), resolvedParams.rawTypes());

                if (method.getReturnType().equals(IStream.class)) {
                    var returnStream = (IStream<?>) method.invoke(service, params);
                    if (returnStream == null){
                        answerParcel.setPayload(new WSStream("stream is null", true, false));
                        l.warn("stream is null");
                    } else {
                        boundStreams.put(parcel.getUuid(), returnStream);
                        answerParcel.setPayload(new WSStream("ok", false, true));
                        l.debug("stream is bound");

                        returnStream.setAccumulator(chunk -> {
                            WSParcel p = new WSParcel();
                            p.setUuid(parcel.getUuid());
                            p.setFrom(name);
                            p.setTo(parcel.getFrom());

                            var data = chunk.first.data;
                            var ps = new WSStream(
                                    data,
                                    chunk.first.isDone,
                                    false
                            );
                            ps.setType(data.getClass().getName());
                            p.setPayload(ps);
                            socket.send(p);

                            if (chunk.first.isDone) boundStreams.remove(parcel.getUuid());
                            return null;
                        });

                        AsyncExecutor.register(returnStream::start);
                    }
                } else {
                    answerPayload.setData(GSON.toJson(method.invoke(service, params)));
                }

                answerPayload.setType(resolveReturnType(method, clazz, Map.of()));
                answerPayload.setSuccess(true);

                if (method.getReturnType() != Void.TYPE) {
                    socket.send(answerParcel);
                }
            } catch (Exception e) {
                l.err(e.toString(), Arrays.toString(e.getStackTrace()), e);
                answerPayload.setSuccess(false);
                answerPayload.setData(GSON.toJson(e.getMessage()));
                answerPayload.setType(String.class.getName());
                socket.send(answerParcel);
            }

        } else {
            l.err("wrong payload [" + parcel.getPayloadType() + "] in WSSParcelRouter");
        }
    }

    private static String resolveReturnType(Method method, Class<?> iface, Map<Class<?>, Class<?>> overrides) {
        Type returnTypeGeneric = method.getGenericReturnType();

        if (returnTypeGeneric instanceof TypeVariable<?> tv) {
            Type resolved = resolveTypeVariable(tv, iface);
            return resolved != null ? normalizeTypeName(resolved) : classToName(method.getReturnType());
        }

        if (returnTypeGeneric instanceof ParameterizedType pt) {
            return resolveParameterizedType(pt, iface, overrides);
        }

        Class<?> ret = method.getReturnType();
        return classToName(overrides.getOrDefault(ret, ret));
    }

    private static Method resolveMethod(Class<?> clazz, String methodName, Class<?>[] argTypes)
            throws NoSuchMethodException {

        // 1. Сначала пробуем точное совпадение (быстрый путь)
        try {
            return clazz.getMethod(methodName, argTypes);
        } catch (NoSuchMethodException ignored) {}

        // 2. Ищем по имени + количеству аргументов + assignability
        for (Method m : clazz.getMethods()) {
            if (!m.getName().equals(methodName)) continue;

            Class<?>[] params = m.getParameterTypes();
            if (params.length != argTypes.length) continue;

            boolean match = true;
            for (int i = 0; i < params.length; i++) {
                // UUID assignable to Serializable — ок
                // Serializable assignable to UUID — нет
                if (!params[i].isAssignableFrom(argTypes[i])) {
                    match = false;
                    break;
                }
            }
            if (match) return m;
        }

        throw new NoSuchMethodException(
                clazz.getName() + "." + methodName + Arrays.toString(argTypes)
        );
    }

    private static String resolveParameterizedType(ParameterizedType pt, Class<?> iface, Map<Class<?>, Class<?>> overrides) {
        Class<?> raw = (Class<?>) pt.getRawType();
        Class<?> effectiveRaw = overrides.getOrDefault(raw, raw); // заменяем только если есть в map

        StringBuilder sb = new StringBuilder();
        sb.append(classToName(effectiveRaw)).append("<");

        Type[] args = pt.getActualTypeArguments();
        for (int i = 0; i < args.length; i++) {
            if (i > 0) sb.append(", ");
            Type arg = args[i];

            if (arg instanceof TypeVariable<?> tv) {
                Type resolved = resolveTypeVariable(tv, iface);
                sb.append(resolved != null ? normalizeTypeName(resolved) : tv.getName());
            } else if (arg instanceof ParameterizedType nested) {
                sb.append(resolveParameterizedType(nested, iface, overrides));
            } else {
                sb.append(normalizeTypeName(arg));
            }
        }

        return sb.append(">").toString();
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

    private static String normalizeTypeName(Type type) {
        return type.getTypeName().replace('$', '.');
    }

    private static String classToName(Class<?> clazz) {
        return clazz.getName().replace('$', '.');
    }

    private record ResolvedParams(Class<?>[] rawTypes, java.lang.reflect.Type[] gsonTypes) {}

    private ResolvedParams resolveParamTypes(String[] paramTypes) throws ClassNotFoundException {
        if (paramTypes == null)
            return new ResolvedParams(new Class<?>[0], new java.lang.reflect.Type[0]);

        Class<?>[] rawTypes = new Class<?>[paramTypes.length];
        java.lang.reflect.Type[] gsonTypes = new java.lang.reflect.Type[paramTypes.length];

        for (int i = 0; i < paramTypes.length; i++) {
            WSSResponseRouter.ResolvedType resolved = decodeTypeString(paramTypes[i], getClass().getClassLoader());
            rawTypes[i] = resolved.rawClass();
            gsonTypes[i] = toGsonType(resolved);
        }

        return new ResolvedParams(rawTypes, gsonTypes);
    }

    private Object[] resolveParams(String[] params, java.lang.reflect.Type[] types) {
        if (params == null)
            return new Object[0];

        Object[] objects = new Object[params.length];
        for (int i = 0; i < params.length; i++) {
            objects[i] = GSON.fromJson(params[i], types[i]);
        }
        return objects;
    }

    private static java.lang.reflect.Type toGsonType(WSSResponseRouter.ResolvedType resolved) {
        if (!resolved.isParameterized()) {
            return resolved.rawClass();
        }

        Type[] argTypes = resolved.typeArgs().stream()
                .map(WSSParcelRouter::toGsonType)
                .toArray(Type[]::new);

        return TypeToken.getParameterized(resolved.rawClass(), argTypes).getType();
    }

    public static WSSResponseRouter.ResolvedType decodeTypeString(String typeName, ClassLoader cl) throws ClassNotFoundException {
        typeName = typeName.trim();

        int lt = typeName.indexOf('<');
        if (lt == -1) {
            // простой тип
            Class<?> clazz = loadClass(typeName, cl);
            return new WSSResponseRouter.ResolvedType(clazz, Collections.emptyList());
        }

        String rawName = typeName.substring(0, lt);
        String argsStr = typeName.substring(lt + 1, typeName.length() - 1); // убираем < >

        Class<?> rawClass = loadClass(rawName, cl);
        List<WSSResponseRouter.ResolvedType> args = splitTypeArgs(argsStr).stream()
                .map(arg -> {
                    try {
                        return decodeTypeString(arg, cl);
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                })
                .toList();

        return new WSSResponseRouter.ResolvedType(rawClass, args);
    }

    private static List<String> splitTypeArgs(String args) {
        List<String> result = new ArrayList<>();
        int depth = 0;
        int start = 0;
        for (int i = 0; i < args.length(); i++) {
            char c = args.charAt(i);
            if (c == '<') depth++;
            else if (c == '>') depth--;
            else if (c == ',' && depth == 0) {
                result.add(args.substring(start, i).trim());
                start = i + 1;
            }
        }
        result.add(args.substring(start).trim());
        return result;
    }

    private static Class<?> loadClass(String name, ClassLoader cl) throws ClassNotFoundException {
        try {
            return Class.forName(name, false, cl);
        } catch (ClassNotFoundException ignored) {}

        char[] chars = name.toCharArray();
        for (int i = chars.length - 1; i >= 0; i--) {
            if (chars[i] == '.') {
                chars[i] = '$';
                try {
                    return Class.forName(new String(chars), false, cl);
                } catch (ClassNotFoundException ignored) {}
            }
        }

        throw new ClassNotFoundException("Cannot resolve class: " + name);
    }
}

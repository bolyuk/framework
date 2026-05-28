package bl0.bjs.socket.services.proxy;

import bl0.bjs.common.async.stream.chunk.StreamChunk;
import bl0.bjs.common.base.BJSBaseClass;
import bl0.bjs.common.base.IContext;
import bl0.bjs.common.core.time.Timer;
import bl0.bjs.socket.base.IResponseAwaiter;
import bl0.bjs.socket.core.parcel.WSParcel;
import bl0.bjs.socket.core.parcel.payload.WSSResponse;
import bl0.bjs.socket.core.parcel.payload.WSStream;
import bl0.bjs.socket.services.proxy.stream.RemoteStreamProxy;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class WSSResponseRouter extends BJSBaseClass implements IResponseAwaiter {

    private final Gson gson = new Gson();
    private final ConcurrentHashMap<UUID, AwaitState> awaiter = new ConcurrentHashMap<>();

    private final Object lock = new Object();

    public Thread wsThread;

    public WSSResponseRouter(IContext ctx) {
        super(ctx);
    }

    public void prepare(UUID uuid) {
        synchronized (lock) {
            AwaitState s = new AwaitState();
            s.isStream = false;
            awaiter.put(uuid, s);
        }
    }

    public void prepareStream(RemoteStreamProxy<?> sp) {
        synchronized (lock) {
            AwaitState s = new AwaitState();
            s.isStream = true;
            s.stream = sp;
            awaiter.put(sp.uuid, s);
        }
    }

    public Object await(UUID uuid) throws InterruptedException {
        throwIfWsThread(wsThread);
        AwaitState s = awaiter.get(uuid);
        if (s == null) throw new IllegalStateException("No awaiters for UUID " + uuid + " was prepared!");
        if (s.isStream) throw new IllegalStateException("Use awaitStream for stream " + uuid);
        if (s.result != null) {
            awaiter.remove(uuid);
            return s.result;
        }

        s.timer.start();
        boolean ok = s.latch.await(10, TimeUnit.SECONDS);
        awaiter.remove(uuid);
        if (!ok)
            l.debug("WSS request [" + uuid + "] failed");
        return ok ? s.result : null;
    }

    public void awaitStream(UUID uuid) throws InterruptedException {
        throwIfWsThread(wsThread);
        AwaitState s = awaiter.get(uuid);
        if (s == null) throw new IllegalStateException("No awaiters for UUID " + uuid + " was prepared!");
        if (!s.isStream) throw new IllegalStateException("not a stream " + uuid);
        s.timer.start();
        s.latch.await();
    }

    public boolean pass(WSParcel parcel) {
        if (!(parcel.getPayload() instanceof WSSResponse response)) {
            l.err("wrong payload [" + parcel.getPayloadType() + "] in ResponseRouter");
            return false;
        }
        AwaitState s = awaiter.get(parcel.getUuid());
        if (s == null) {
            return false;
        }

        if (response.getType() == null)
            return false;

        Object value;
        try {
            if (response.isSuccess()){
                java.lang.reflect.Type gsonType = toGsonType(
                        decodeTypeString(response.getType(), getClass().getClassLoader())
                );

                value = gson.fromJson(response.getData(), gsonType);
            }
            else
                value = new WSException(response.getData());
        } catch (ClassNotFoundException e) {
            l.err("response class [" + response.getType() + "] was not found");
            return false;
        }

        if (s.isStream) {
            return handleStream(s, response, value, parcel.getUuid());
        } else {
            l.debug("took: " + s.timer.stop() + "ms");
            s.result = value;
            s.latch.countDown();
        }
        return true;
    }

    private boolean handleStream(AwaitState s, WSSResponse response, Object value, UUID uuid) {
        if (!(response instanceof WSStream stream)) {
            s.stream.feedGeneric(new StreamChunk<>("Expected WSStream, got " + response.getClass().getSimpleName()));
            s.latch.countDown();
            return true;
        }

        if (stream.isACK) {
            l.debug("stream ack");
            s.latch.countDown();
            return true;
        }

        StreamChunk<?> res;
        if (response.isSuccess())
            res = new StreamChunk<>(stream.isDone, value);
        else
            res = new StreamChunk<>(response.getData());
        s.stream.feedGeneric(res);

        if (stream.isDone) {
            awaiter.remove(uuid);
            l.debug("stream done");
        }

        return true;
    }

    static final class AwaitState {
        final Timer timer = new Timer();
        final CountDownLatch latch = new CountDownLatch(1);
        volatile Object result;
        volatile RemoteStreamProxy<?> stream;
        volatile boolean isStream;
    }

    private static void throwIfWsThread(Thread wsThread) {
        if (wsThread == null)
            return;

        if (wsThread == Thread.currentThread()) {
            throw new IllegalStateException(
                    "Blocking operation is not permitted in WS thread [" +
                            wsThread.getName() + "]"
            );
        }
    }

    private static java.lang.reflect.Type toGsonType(ResolvedType resolved) {
        if (!resolved.isParameterized()) {
            return resolved.rawClass();
        }

        Type[] argTypes = resolved.typeArgs().stream()
                .map(WSSResponseRouter::toGsonType)
                .toArray(Type[]::new);

        return TypeToken.getParameterized(resolved.rawClass(), argTypes).getType();
    }

    public static ResolvedType decodeTypeString(String typeName, ClassLoader cl) throws ClassNotFoundException {
        typeName = typeName.trim();

        int lt = typeName.indexOf('<');
        if (lt == -1) {
            // простой тип
            Class<?> clazz = loadClass(typeName, cl);
            return new ResolvedType(clazz, Collections.emptyList());
        }

        String rawName = typeName.substring(0, lt);
        String argsStr = typeName.substring(lt + 1, typeName.length() - 1); // убираем < >

        Class<?> rawClass = loadClass(rawName, cl);
        List<ResolvedType> args = splitTypeArgs(argsStr).stream()
                .map(arg -> {
                    try {
                        return decodeTypeString(arg, cl);
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                })
                .toList();

        return new ResolvedType(rawClass, args);
    }

    /** Разбивает "A<B<C>, D>" на ["A<B<C>>", "D"] — учитывает вложенность. */
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

        // Перебираем все точки справа налево, заменяя на $
        // IStreamDataSource.RangeChunk -> IStreamDataSource$RangeChunk
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

    /** Простой контейнер результата. */
    public record ResolvedType(Class<?> rawClass, List<ResolvedType> typeArgs) {
        public boolean isParameterized() { return !typeArgs.isEmpty(); }

        /** Восстанавливает строку обратно (для логов/отладки). */
        @Override
        public String toString() {
            if (typeArgs.isEmpty()) return rawClass.getName().replace('$', '.');
            return rawClass.getName().replace('$', '.') + "<" +
                    typeArgs.stream().map(ResolvedType::toString).collect(Collectors.joining(", ")) +
                    ">";
        }
    }
}

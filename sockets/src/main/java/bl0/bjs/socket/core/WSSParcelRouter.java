package bl0.bjs.socket.core;

import bl0.bjs.common.base.BJSBaseClass;
import bl0.bjs.common.base.IContext;
import bl0.bjs.async.queue.Queue;
import bl0.bjs.common.core.tuple.Pair;
import bl0.bjs.socket.services.IWebSocketService;
import bl0.bjs.socket.services.proxy.WSSResponse;
import bl0.bjs.socket.services.proxy.WSSParcel;
import org.java_websocket.WebSocket;

import java.lang.reflect.Method;
import java.util.List;

import static bl0.bjs.socket.C.GSON;

public class WSSParcelRouter extends BJSBaseClass {

    private final Queue<Pair<WebSocket, WSSParcel>> queue;

    public WSSParcelRouter(IContext ctx) {
        super(ctx);
        queue = new Queue<>(ctx,this::exec);
        queue.setMaxBatchSize(1);
    }

    public void pass(WSSParcel parcel, WebSocket socket) {
        queue.pass(List.of(Pair.of(socket,parcel)));
    }

    private void exec(Queue<Pair<WebSocket, WSSParcel>> queue, List<Pair<WebSocket, WSSParcel>> list){
        WebSocket socket = list.getFirst().first;

        WSSParcel parcel = list.getFirst().second;
        WSSResponse answer = new WSSResponse();

        answer.setUuid(parcel.getUuid());

        try {
            Class<?> clazz = Class.forName(parcel.getPath());

            if (!IWebSocketService.class.isAssignableFrom(clazz))
                throw new ClassCastException(parcel.getPath()+" is not assignable to IWebSocketService");

            Object service = ctx.getServiceContainer().getService((Class<? extends IWebSocketService>) clazz);

            if(service == null)
                throw new NullPointerException(parcel.getPath()+" does not exist");


            Class<?>[] paramTypes = resolveParamTypes(parcel.getParamTypes());
            Object[] params = resolveParams(parcel.getParams(),  paramTypes);

            Method method = clazz.getMethod(parcel.getMethod(), paramTypes);

            answer.setData(GSON.toJson(method.invoke(service,params)));
            answer.setType(method.getReturnType().getName());

            if (method.getReturnType() != Void.TYPE) {
                socket.send(GSON.toJson(answer));
            }
        } catch (Exception e){
            answer.setData(GSON.toJson(e));
            answer.setType(e.getClass().getName());
            socket.send(GSON.toJson(answer));
        }
    }

    private Class<?>[] resolveParamTypes(String[] paramTypes) throws ClassNotFoundException {
        if (paramTypes == null)
            return new Class<?>[0];

        Class<?>[] types = new Class<?>[paramTypes.length];
        for (int i = 0; i < paramTypes.length; i++) {
                types[i] = Class.forName(paramTypes[i]);
        }
        return types;
    }

    private Object[] resolveParams(String[] params, Class<?>[] types){
        if (params == null)
            return new Object[0];

        Object[] objects = new Object[params.length];
        for (int i = 0; i < params.length; i++) {
            objects[i] = GSON.fromJson(params[i], types[i]);
        }
        return objects;
    }
}

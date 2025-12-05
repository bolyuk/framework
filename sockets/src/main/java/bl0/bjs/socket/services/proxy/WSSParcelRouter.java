package bl0.bjs.socket.services.proxy;

import bl0.bjs.common.base.BJSBaseClass;
import bl0.bjs.common.base.IContext;
import bl0.bjs.socket.core.data.NamedSocket;
import bl0.bjs.socket.core.parcel.WSParcel;
import bl0.bjs.socket.services.IWebSocketService;
import bl0.bjs.socket.core.parcel.payload.WSSResponse;
import bl0.bjs.socket.core.parcel.payload.WSSRequest;

import java.lang.reflect.Method;

import static bl0.bjs.socket.C.GSON;

public class WSSParcelRouter extends BJSBaseClass {

    private final String name;

    public WSSParcelRouter(IContext ctx, String name) {
        super(ctx);
        this.name = name;
    }

    public void feed(WSParcel parcel, NamedSocket socket){
        if(parcel.getPayload() instanceof WSSRequest request){

        WSParcel answerParcel = new WSParcel();
        WSSResponse answerPayload = new WSSResponse();
        answerParcel.setPayload(answerPayload);
        answerParcel.setTo(parcel.getFrom());
        answerParcel.setFrom(name);
        answerParcel.setUuid(parcel.getUuid());

        try {
            Class<?> clazz = Class.forName(request.getPath());

            if (!IWebSocketService.class.isAssignableFrom(clazz))
                throw new ClassCastException(request.getPath()+" is not assignable to IWebSocketService");

            Object service = ctx.getServiceContainer().getService((Class<? extends IWebSocketService>) clazz);

            if(service == null)
                throw new NullPointerException(request.getPath()+" does not exist");


            Class<?>[] paramTypes = resolveParamTypes(request.getParamTypes());
            Object[] params = resolveParams(request.getParams(),  paramTypes);

            Method method = clazz.getMethod(request.getMethod(), paramTypes);

            answerPayload.setData(GSON.toJson(method.invoke(service,params)));
            answerPayload.setType(method.getReturnType().getName());
            answerPayload.setSuccess(true);

            if (method.getReturnType() != Void.TYPE) {
                socket.send(answerParcel);
            }
        } catch (Exception e){
            l.err(e);
            answerPayload.setSuccess(false);
            answerPayload.setData(GSON.toJson(e.getMessage()));
            answerPayload.setType(String.class.getName());
            socket.send(answerParcel);
        }

        } else {
            l.err("wrong payload ["+parcel.getPayloadType()+"] in WSSParcelRouter");
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

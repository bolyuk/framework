package bl0.bjs.socket.utils;

import bl0.bjs.logging.ILogger;
import bl0.bjs.socket.core.NamedSocket;
import bl0.bjs.socket.core.data.WSBaseParcel;
import bl0.bjs.socket.core.data.WSSResponse;

import java.util.UUID;

import static bl0.bjs.socket.C.GSON;

public class ParcelUtils {
    public static <T> T parse(String json, Class<T> classOfT) {
        return GSON.fromJson(json, classOfT);
    }

    public static <T extends WSBaseParcel> T tryParse(String json, NamedSocket socket, ILogger l, String name) {
        WSBaseParcel bParcel = GSON.fromJson(json, WSBaseParcel.class);

        if(bParcel == null){
            sendParcelErrorBackAndLog(ParcelErrors.INVALID_DATA, json, socket, l, name);
            return null;
        }

        if(bParcel.getUuid() == null){
            sendParcelErrorBackAndLog(ParcelErrors.INVALID_UUID, json, socket, l, name);
            return null;
        }

        if(bParcel.getParcelType() == null){
            sendParcelErrorBackAndLog(ParcelErrors.INVALID_TYPE, json, socket, l, name);
            return null;
        }

        if(bParcel.getFrom() == null){
            sendParcelErrorBackAndLog(ParcelErrors.INVALID_SENDER, json, socket, l, name);
            return null;
        }

        try {
            Class<?> clazz = Class.forName(bParcel.getParcelType());
            return (T) GSON.fromJson(json, clazz);
        } catch (ClassNotFoundException e){
            sendParcelErrorBackAndLog("parcel type was not found ["+bParcel.getParcelType()+"]", null, socket, l, name);
        }
        return null;
    }

    public static void sendParcelErrorBackAndLog(String error, String extra_data, NamedSocket socket, ILogger l, String name){
        WSSResponse  response = new WSSResponse();
        response.setSuccess(false);
        response.setData(error);
        response.setTo(socket.getName());
        response.setFrom(name);
        response.setUuid(UUID.randomUUID());
        response.setType(String.class.toString());
        l.warn("Parcel Error: "+error, extra_data);

        if(socket != null)
            socket.send(GSON.toJson(response));
    }
}

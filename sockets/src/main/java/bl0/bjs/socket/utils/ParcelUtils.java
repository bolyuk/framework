package bl0.bjs.socket.utils;

import bl0.bjs.logging.ILogger;
import bl0.bjs.socket.core.data.NamedSocket;
import bl0.bjs.socket.core.data.WSParcel;
import bl0.bjs.socket.core.payload.IPayload;
import bl0.bjs.socket.core.payload.WSSResponse;

import java.util.UUID;

import static bl0.bjs.socket.C.GSON;

public class ParcelUtils {
    public static <T> T parse(String json, Class<T> classOfT) {
        return GSON.fromJson(json, classOfT);
    }

    public static WSParcel tryParse(String json, NamedSocket socket, ILogger l, String name) {
        WSParcel bParcel = GSON.fromJson(json, WSParcel.class);

        if(bParcel == null){
            sendParcelErrorBackAndLog(ParcelErrors.INVALID_DATA, json, socket, l, name);
            return null;
        }

        if(bParcel.getUuid() == null){
            sendParcelErrorBackAndLog(ParcelErrors.INVALID_UUID, json, socket, l, name);
            return null;
        }

        if(bParcel.getPayloadType() == null){
            sendParcelErrorBackAndLog(ParcelErrors.INVALID_TYPE, json, socket, l, name);
            return null;
        }

        if(bParcel.getFrom() == null){
            sendParcelErrorBackAndLog(ParcelErrors.INVALID_SENDER, json, socket, l, name);
            return null;
        }

        try {
            Class<?> clazz = Class.forName(bParcel.getPayloadType());
            bParcel.setPayload((IPayload)GSON.fromJson(bParcel.getPayloadString(), clazz));
            return bParcel;
        } catch (ClassNotFoundException e){
            sendParcelErrorBackAndLog("payload type was not found ["+bParcel.getPayloadType()+"]", null, socket, l, name);
        }
        return null;
    }

    public static void sendParcelErrorBackAndLog(String error, String extra_data, NamedSocket socket, ILogger l, String name){
        WSParcel parcel = new WSParcel();
        parcel.setTo(socket.getName());
        parcel.setFrom(name);
        parcel.setUuid(UUID.randomUUID());

        WSSResponse payload = new WSSResponse();

        payload.setSuccess(false);
        payload.setData(error);
        payload.setType(String.class.toString());

        parcel.setPayload(payload);
        l.warn("Parcel Error: "+error, extra_data);
        socket.send(parcel);
    }
}

package bl0.bjs.socket.core.parcel.payload;

import com.google.gson.Gson;

public class WSSEvent implements IPayload {
    String data;
    String dataType;

    public WSSEvent(Object data, Gson gson) {
        this.data = gson.toJson(data);
        this.dataType = data.getClass().getName();
    }
}

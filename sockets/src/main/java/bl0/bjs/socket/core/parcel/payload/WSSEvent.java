package bl0.bjs.socket.core.parcel.payload;

import static bl0.bjs.socket.C.GSON;

public class WSSEvent implements IPayload {
    String data;
    String dataType;

    public WSSEvent(Object data) {
        this.data = GSON.toJson(data);
        this.dataType = data.getClass().getName();
    }
}

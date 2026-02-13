package bl0.bjs.socket.core.parcel.payload;

import static bl0.bjs.socket.C.GSON;

public class WSStream extends WSSResponse {
    public final boolean isDone;
    public final boolean isACK;

    public WSStream(Object data, boolean done, boolean ack) {
        this.data = GSON.toJson(data);
        this.type = data.getClass().getName();
        this.isDone = done;
        this.isACK = ack;
        this.isSuccess = true;
    }

    public WSStream() {
        this("ack", false, true);
    }
}

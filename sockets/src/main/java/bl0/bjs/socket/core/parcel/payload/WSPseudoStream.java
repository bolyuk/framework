package bl0.bjs.socket.core.parcel.payload;

import static bl0.bjs.socket.C.GSON;

public class WSPseudoStream extends WSSResponse {
    public final boolean isDone;

    public WSPseudoStream(Object data, boolean done) {
        this.data = GSON.toJson(data);
        this.type = data.getClass().getName();
        this.isDone = done;
    }
}

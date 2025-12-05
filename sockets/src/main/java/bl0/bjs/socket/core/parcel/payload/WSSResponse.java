package bl0.bjs.socket.core.parcel.payload;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class WSSResponse implements IPayload {
    String data;
    String type;

    boolean isSuccess;
}

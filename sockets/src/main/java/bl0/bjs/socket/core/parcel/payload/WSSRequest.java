package bl0.bjs.socket.core.parcel.payload;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class WSSRequest implements IPayload {
    String path;
    String method;

    String[] params;
    String[] paramTypes;
}

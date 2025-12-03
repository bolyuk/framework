package bl0.bjs.socket.core.payload;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class WSSRequest implements IPayload {
    private String path;
    private String method;

    private String[] params;
    private String[] paramTypes;
}

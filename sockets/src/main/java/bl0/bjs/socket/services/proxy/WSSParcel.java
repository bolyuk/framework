package bl0.bjs.socket.services.proxy;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter @Setter
public class WSSParcel {
    private String path;
    private UUID uuid;
    private String method;
    private String[] params;
    private String[] paramTypes;
}

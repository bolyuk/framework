package bl0.bjs.socket.core.data;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter @Setter
public class WSSParcel {
    private UUID uuid;

    private String from;
    private String to;

    private String path;
    private String method;

    private String[] params;
    private String[] paramTypes;
}

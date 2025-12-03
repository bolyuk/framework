package bl0.bjs.socket.core.data;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter @Setter
public class WSSParcel extends WSBaseParcel {

    private String path;
    private String method;

    private String[] params;
    private String[] paramTypes;
}

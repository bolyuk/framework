package bl0.bjs.socket.core.data;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter @Setter
public class WSSResponse {
    private String to;

    UUID uuid;
    String data;
    String type;
    boolean isSuccess;
}

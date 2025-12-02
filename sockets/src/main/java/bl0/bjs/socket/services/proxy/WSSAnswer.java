package bl0.bjs.socket.services.proxy;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter @Setter
public class WSSAnswer {
    UUID uuid;
    String data;
    String type;
}

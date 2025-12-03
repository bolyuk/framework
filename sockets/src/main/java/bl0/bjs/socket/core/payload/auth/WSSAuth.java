package bl0.bjs.socket.core.payload.auth;

import bl0.bjs.socket.core.payload.IPayload;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class WSSAuth implements IPayload {
    String name;
    String[] services;
    String publicToken;
}

package bl0.bjs.socket.core.parcel.payload.auth;

import bl0.bjs.socket.core.parcel.payload.IPayload;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class WSSAuth implements IPayload {
    String name;
    String[] services;
    String publicToken;
}

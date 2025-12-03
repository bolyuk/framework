package bl0.bjs.socket.core.data;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class WSSRegParcel extends WSBaseParcel {
    String name;
    String[] services;
}

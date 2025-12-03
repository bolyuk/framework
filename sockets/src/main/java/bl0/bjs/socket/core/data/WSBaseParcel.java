package bl0.bjs.socket.core.data;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter @Setter
public class WSBaseParcel {
    private final String parcelType;
    private UUID uuid;

    private String from;
    private String to;

    public WSBaseParcel(){
        this.parcelType = this.getClass().getName();
    }
}

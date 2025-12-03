package bl0.bjs.socket.core.data;

import bl0.bjs.socket.core.payload.IPayload;
import bl0.bjs.socket.core.payload.auth.WSCredentials;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

import static bl0.bjs.socket.C.GSON;

@Getter @Setter
public class WSParcel {
    private String payloadType;
    private UUID uuid;

    private String from;
    private String to;

    private transient IPayload payload;
    private String payloadString;

    private WSCredentials credentials;

    public void setPayload(IPayload payload) {
        this.payload = payload;
        payloadType = payload.getClass().getName();
    }

    public void beforeSend() {
        payloadString = GSON.toJson(payload);
    }
}

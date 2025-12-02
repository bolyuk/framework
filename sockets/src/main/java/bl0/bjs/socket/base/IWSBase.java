package bl0.bjs.socket.base;

import bl0.bjs.socket.services.IWebSocketService;

import java.util.List;

public interface IWSBase {
    <T extends IWebSocketService> T get(Class<T> service);

    <T extends IWebSocketService> T getNamed(Class<T> service, String name);
    <T extends IWebSocketService> List<T> getAll(Class<T> service);
}

package bl0.bjs.socket.base;

import bl0.bjs.eventbus.IEventBus;
import bl0.bjs.eventbus.IEventBusController;
import bl0.bjs.eventbus.IEventBusNode;
import bl0.bjs.socket.services.IWebSocketService;

import java.util.List;

public interface IWSBase {
    <T extends IWebSocketService> T get(Class<T> service);

    <T extends IWebSocketService> T getNamed(Class<T> service, String name);
    <T extends IWebSocketService> List<T> getAll(Class<T> service);

    <T extends IEventBusNode<T>> void connectEventBus(Class<T> dataClass);

    String getName();

}

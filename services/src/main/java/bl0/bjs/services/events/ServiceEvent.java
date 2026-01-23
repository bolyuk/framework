package bl0.bjs.services.events;

import bl0.bjs.eventbus.IEventBusNode;
import bl0.bjs.services.interfaces.IService;

public interface ServiceEvent extends IEventBusNode<ServiceEvent.ServicePayload> {
    record ServicePayload(Class<? extends IService> service, EventType event) {
    }

    enum EventType {REGISTERED, UNREGISTERED}
}

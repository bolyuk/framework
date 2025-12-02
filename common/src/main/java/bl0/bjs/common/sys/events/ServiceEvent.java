package bl0.bjs.common.sys.events;

import bl0.bjs.common.base.eventbus.IEventBusNode;
import bl0.bjs.common.base.service.interfaces.IService;

public interface ServiceEvent extends IEventBusNode<ServiceEvent.ServicePayload> {
    public record ServicePayload(Class<? extends IService> service, EventType event){}
    public enum EventType { REGISTERED, UNREGISTERED }
}

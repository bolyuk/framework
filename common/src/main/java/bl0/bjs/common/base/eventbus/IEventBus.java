package bl0.bjs.common.base.eventbus;

public interface IEventBus {
    public <T extends IEventBusNode<R>,R> IEventBusController<T, R> getController(Class<T> clazz);
}

package bl0.bjs.eventbus;

public interface IEventBus {
    <T extends IEventBusNode<R>,R> IEventBusController<T, R> getController(Class<T> clazz);
}

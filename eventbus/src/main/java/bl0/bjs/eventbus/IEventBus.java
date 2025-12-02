package bl0.bjs.eventbus;

public interface IEventBus {
    public <T extends IEventBusNode<R>,R> IEventBusController<T, R> getController(Class<T> clazz);
}

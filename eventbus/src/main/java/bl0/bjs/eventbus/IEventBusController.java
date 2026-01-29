package bl0.bjs.eventbus;

public interface IEventBusController<T extends IEventBusNode<R>, R> {
    void subscribe(T node);

    void subscribeGeneric(IEventBusNode<R> node);

    void unsubscribe(T node);

    void fireEvent(R data);
}

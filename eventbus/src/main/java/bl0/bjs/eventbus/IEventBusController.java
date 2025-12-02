package bl0.bjs.eventbus;

public interface IEventBusController<T extends IEventBusNode<R>, R> {
    public void subscribe(T node);
    public void unsubscribe(T node);
    public void fireEvent(R data);
}

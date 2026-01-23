package bl0.bjs.eventbus;

public interface IEventBusNode<R> {
    void onEvent(R data);
}

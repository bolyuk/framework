package bl0.bjs.eventbus;

@FunctionalInterface
public interface IEventBusNode<R> {
    void onEvent(R data);
}

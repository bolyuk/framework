package bl0.bjs.eventbus;

public interface IEventBusNode<R> {
    public void onEvent(R data);
}

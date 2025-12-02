package bl0.bjs.common.base.eventbus;

public interface IEventBusNode<R> {
    public void onEvent(R data);
}

package bl0.bjs.common.core.event;

public interface Event<G, T> {
    T onEvent(G data);
}

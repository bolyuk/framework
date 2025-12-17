package bl0.bjs.common.core.event;

@FunctionalInterface
public interface Event<G, T> {
    T invoke(G data);
}

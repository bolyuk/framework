package bl0.bjs.common.core.tuple;

@FunctionalInterface
public interface TriConsumer<A,B,C> {
    void accept(A a, B b, C c);
}

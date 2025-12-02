package bl0.bjs.common.core.tuple;

public class Quadruple<T, U, V, W> {
    private final T first;
    private final U second;
    private final V third;
    private final W fourth;

    public Quadruple(T first, U second, V third, W fourth) {
        this.first = first;
        this.second = second;
        this.third = third;
        this.fourth = fourth;
    }

    public T getFirst() {
        return first;
    }

    public U getSecond() {
        return second;
    }

    public V getThird() {
        return third;
    }

    public W getFourth() {
        return fourth;
    }

    @Override
    public String toString() {
        return "(" + first.toString() + ", " + second.toString() + ", " + third.toString() + ", " + fourth.toString() + ")";
    }
}

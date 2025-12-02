package bl0.bjs.common.core.tuple;

public class Pair<T, C> {
    public final T first;
    public final C second;

    public Pair(T first, C second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public String toString() {
        return "(" + first.toString() + ", " + second.toString() + ")";
    }

    public static <T, C> Pair<T, C> of(T first, C second) {
        return new Pair<T, C>(first, second);
    }
}


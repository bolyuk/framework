package bl0.bjs.common.core.tuple;

public class Triple<T, C, K> {
    public final T first;
    public final C second;
    public final K third;

    public Triple(T first, C second, K third) {
        this.first = first;
        this.second = second;
        this.third = third;
    }

    @Override
    public String toString() {
        return "(" + first + ", " + second + ", " + third + ")";
    }

    public static <T,C,K> Triple<T,C,K> of(T first, C second, K third) {
        return new Triple<T,C,K>(first, second, third);
    }
}


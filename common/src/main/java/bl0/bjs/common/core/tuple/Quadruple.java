package bl0.bjs.common.core.tuple;

public record Quadruple<T, U, V, W>(T first, U second, V third, W fourth) {

    @Override
    public String toString() {
        return "(" + first.toString() + ", " + second.toString() + ", " + third.toString() + ", " + fourth.toString() + ")";
    }
}

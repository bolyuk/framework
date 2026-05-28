package bl0.bjs.common.async.stream.data;

public record DataInfo<DATA>(DataState state, DATA data) {

    public static <T> DataInfo<T> loaded(T data) {
        return new DataInfo<>(DataState.LOADED, data);
    }

    public static <T> DataInfo<T> placeholder() {
        return new DataInfo<>(DataState.PLACEHOLDER, null);
    }

    public static <T> DataInfo<T> error() {
        return new DataInfo<>(DataState.ERROR, null);
    }
}

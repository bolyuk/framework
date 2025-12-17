package bl0.bjs.common.core.event;

@FunctionalInterface
public interface Action<T> {
     void invoke(T data);
}

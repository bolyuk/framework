package bl0.bjs.common.core.event;

@FunctionalInterface
public interface Action<T> {
     void onAction(T data);
}

package bl0.bjs.common.core.event;

public interface IActionController<T> {
    void register(Action<T> action);
    void unregister(Action<T> action);
}

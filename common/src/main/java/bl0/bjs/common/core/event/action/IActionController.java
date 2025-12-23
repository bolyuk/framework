package bl0.bjs.common.core.event.action;

public interface IActionController<T, R extends Action<T>> {
    void register(R action);
    void unregister(R action);
    void registerSingle(TaggedAction<T> tagged);
}

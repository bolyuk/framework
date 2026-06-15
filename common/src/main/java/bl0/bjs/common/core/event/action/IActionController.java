package bl0.bjs.common.core.event.action;

public interface IActionController<T, R extends Action<T>> {
    void schedule(int delay, Action<T> action);
    void register(R action);
    void unregister(R action);
    void registerUnique(TaggedAction<T> tagged);
}

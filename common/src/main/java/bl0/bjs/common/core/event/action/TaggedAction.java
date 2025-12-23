package bl0.bjs.common.core.event.action;

public interface TaggedAction<T> extends Action<T> {
    String tag();
}

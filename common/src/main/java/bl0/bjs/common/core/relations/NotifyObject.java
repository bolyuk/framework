package bl0.bjs.common.core.relations;

import bl0.bjs.common.core.event.Action;
import bl0.bjs.common.core.event.ActionController;

public class NotifyObject<T> implements INotifier<NotifyObject<T>, T> {
    private T object;
    private final ActionController<T> controller = new ActionController<T>();

    public NotifyObject(T object) {
        this.object = object;
    }

    public T get() {
        return this.object;
    }

    public NotifyObject<T> set(T object) {
        this.object = object;
        this.invoke();
        return this;
    }

    @Override
    public NotifyObject<T> addListener(Action<T> e) {
        controller.register(e);
        return this;
    }

    @Override
    public NotifyObject<T> remListener(Action<T> e) {
        controller.unregister(e);
        return this;
    }

    @Override
    public NotifyObject<T> invoke() {
        controller.invoke(object);
        return this;
    }
}


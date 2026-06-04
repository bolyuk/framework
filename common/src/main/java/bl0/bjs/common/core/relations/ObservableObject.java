package bl0.bjs.common.core.relations;

import bl0.bjs.common.core.event.action.Action;
import bl0.bjs.common.core.event.action.ActionController;

public class ObservableObject<T> implements IObservable<ObservableObject<T>, T> {
    private T object;
    private final ActionController<T> controller = new ActionController<T>();

    public ObservableObject(T object) {
        this.object = object;
    }

    public T get() {
        return this.object;
    }

    public ObservableObject<T> set(T object) {
        this.object = object;
        this.invokeChangeAction();
        return this;
    }

    @Override
    public ObservableObject<T> addListener(Action<T> e) {
        controller.register(e);
        return this;
    }

    @Override
    public ObservableObject<T> remListener(Action<T> e) {
        controller.unregister(e);
        return this;
    }

    @Override
    public ObservableObject<T> invokeChangeAction() {
        controller.invoke(object);
        return this;
    }
}


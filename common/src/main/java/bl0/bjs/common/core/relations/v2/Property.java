package bl0.bjs.common.core.relations.v2;

import bl0.bjs.common.core.event.action.Action;
import bl0.bjs.common.core.event.action.ActionController;
import bl0.bjs.common.core.relations.IObservable;

import java.util.HashMap;

public class Property<T> implements IBindable<Property<T>, T> {

    protected T value;
    protected final ActionController<T> notifier = new ActionController<T>();
    protected final HashMap<IObservable<?, T>, Action<T>> boundListeners = new HashMap<>();

    protected boolean updating = false;

    @Override
    public Property<T> bind(IObservable<Property<T>, T> observable) {
        Action<T> existing = boundListeners.get(observable);
        if (existing != null) observable.remListener(existing);

        set(observable.get());

        Action<T> listener = this::set;
        boundListeners.put(observable, listener);
        observable.addListener(listener);
        return this;
    }

    @Override
    public Property<T> unbind(IObservable<Property<T>, T> observable) {
        Action<T> listener = boundListeners.remove(observable);
        if (listener != null) observable.remListener(listener);
        return this;
    }

    @Override
    public Property<T> bindBidirectional(IBindable<Property<T>, T> observable) {
        bind(observable);
        observable.bind(this);
        return this;
    }

    @Override
    public Property<T> unbindBidirectional(IBindable<Property<T>, T> observable) {
        unbind(observable);
        observable.unbind(this);
        return this;
    }

    @Override
    public Property<T> addListener(Action<T> action) {
        notifier.register(action);
        return this;
    }

    @Override
    public Property<T> remListener(Action<T> action) {
        notifier.unregister(action);
        return this;
    }

    @Override
    public Property<T> invokeChangeAction() {
        set(value);
        return this;
    }

    @Override
    public T get() {
        return value;
    }


    @Override
    public Property<T> set(T value) {
        if (updating) return this; // разрываем цикл
        updating = true;
        try {
            this.value = value;
            notifier.invoke(value);
        } finally {
            updating = false;
        }
        return this;
    }
}

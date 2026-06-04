package bl0.bjs.common.core.relations;

import bl0.bjs.common.core.event.action.Action;
import bl0.bjs.common.core.event.Event;

import java.util.concurrent.ConcurrentLinkedQueue;

public class FactorizedObject<T> {
    private T object;
    private final ConcurrentLinkedQueue<IObservable<?, ?>> bindings = new ConcurrentLinkedQueue<>();
    private final Event<Void, T> factory;
    private boolean isDirty = true;
    private final Action updateListener = e -> this.markDirty();

    private long delay = 5;
    private long lastDelay = 0;

    public FactorizedObject(T object, Event<Void, T> factory, IObservable<?, ?>... bindings) {
        this.object = object;
        this.factory = factory;
        for (IObservable<?, ?> binding : bindings) {
            this.bind(binding);
        }
    }

    public FactorizedObject(T object, Event<Void, T> factory, long delay, IObservable<?, ?>... bindings) {
        this.object = object;
        this.factory = factory;
        this.delay = delay;
        for (IObservable<?, ?> binding : bindings) {
            this.bind(binding);
        }
    }

    public void markDirty() {
        this.isDirty = true;
    }

    public FactorizedObject<T> unbindAll() {
        for (IObservable<?, ?> binding : this.bindings) {
            this.unbind(binding);
        }
        return this;
    }

    public T get() {
        if (this.isDirty && lastDelay+delay < System.currentTimeMillis()) {
            this.object = this.factory.invoke(null);
            lastDelay = System.currentTimeMillis();
        }
        this.isDirty = false;
        return this.object;
    }

    private FactorizedObject<T> bind(IObservable<?, ?> notifier) {
        this.bindings.add(notifier);
        notifier.addListener(this.updateListener);
        return this;
    }

    private FactorizedObject<T> unbind(IObservable<?, ?> notifier) {
        this.bindings.remove(notifier);
        notifier.remListener(this.updateListener);
        return this;
    }
}


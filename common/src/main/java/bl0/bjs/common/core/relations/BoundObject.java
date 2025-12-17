package bl0.bjs.common.core.relations;

import bl0.bjs.common.core.event.Action;
import bl0.bjs.common.core.event.Event;

import java.util.concurrent.ConcurrentLinkedQueue;

public class BoundObject<T> {
    private T object;
    private final ConcurrentLinkedQueue<INotifier<?, ?>> bindings = new ConcurrentLinkedQueue<>();
    private final Event<Void, T> factory;
    private boolean isDirty = true;
    private final Action updateListener = e -> this.markDirty();

    private long delay = 5;
    private long lastDelay = 0;

    public BoundObject(T object, Event<Void, T> factory, INotifier<?, ?> ... bindings) {
        this.object = object;
        this.factory = factory;
        for (INotifier<?, ?> binding : bindings) {
            this.bind(binding);
        }
    }

    public BoundObject(T object, Event<Void, T> factory, long delay, INotifier<?, ?> ... bindings) {
        this.object = object;
        this.factory = factory;
        this.delay = delay;
        for (INotifier<?, ?> binding : bindings) {
            this.bind(binding);
        }
    }

    public void markDirty() {
        this.isDirty = true;
    }

    public BoundObject<T> unbindAll() {
        for (INotifier<?, ?> binding : this.bindings) {
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

    private BoundObject<T> bind(INotifier<?, ?> notifier) {
        this.bindings.add(notifier);
        notifier.addListener(this.updateListener);
        return this;
    }

    private BoundObject<T> unbind(INotifier<?, ?> notifier) {
        this.bindings.remove(notifier);
        notifier.addListener(this.updateListener);
        return this;
    }
}


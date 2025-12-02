package bl0.bjs.common.core.event;

import java.util.concurrent.ConcurrentLinkedQueue;

public class ChainController<T> {
    private final ConcurrentLinkedQueue<Event<T, Boolean>> handlers = new ConcurrentLinkedQueue<>();

    public void addHandler(Event<T, Boolean> handler) {
        handlers.add(handler);
    }

    public void removeHandler(Event<T, Boolean> handler) {
        handlers.remove(handler);
    }

    public void invoke(T data) {
        for (var handler : handlers) {
            if (handler.onEvent(data))
                return;
        }
    }
}

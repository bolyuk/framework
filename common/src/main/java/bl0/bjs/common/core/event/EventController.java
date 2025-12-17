package bl0.bjs.common.core.event;

import java.util.ArrayList;

public class EventController<T, G> {
    private final ArrayList<Event<T, G>> handlers = new ArrayList<>();

    public void addHandler(Event<T, G> handler) {
        handlers.add(handler);
    }

    public void removeHandler(Event<T, G> handler) {
        handlers.remove(handler);
    }

    public void invoke(T data) {
        for (var handler : handlers)
            handler.invoke(data);
    }

    public boolean isEmpty() {
        return handlers.isEmpty();
    }
}

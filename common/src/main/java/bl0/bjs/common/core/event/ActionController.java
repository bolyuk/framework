package bl0.bjs.common.core.event;

import java.util.ArrayList;

public class ActionController<T> implements IActionController<T> {
    protected final ArrayList<Action<T>> actions = new  ArrayList<>();
    @Override
    public void register(Action<T> action) {
        actions.add(action);
    }

    @Override
    public void unregister(Action<T> action) {
        actions.remove(action);
    }

    public void invoke(T data) {
        for(Action<T> action : actions) {
            action.invoke(data);
        }
    }
}

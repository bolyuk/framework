package bl0.bjs.common.core.event.action;

import java.util.ArrayList;
import java.util.HashMap;

public class ActionController<T> implements IActionController<T, Action<T>> {

    protected final ArrayList<Action<T>> actions = new  ArrayList<>();
    protected final HashMap<String, TaggedAction<T>> taggedActions = new HashMap<>();

    @Override
    public void register(Action<T> action) {
        actions.add(action);
    }

    @Override
    public void unregister(Action<T> action) {
        actions.remove(action);
    }

    @Override
    public void registerSingle(TaggedAction<T> tagged) {
        taggedActions.put(tagged.tag(), tagged);
    }

    public void invoke(T data) {
        for(Action<T> action : new ArrayList<>(actions))
            action.invoke(data);

        for(TaggedAction<T> taggedAction : new ArrayList<>(taggedActions.values()))
            taggedAction.invoke(data);

    }

    public void clear(){
        actions.clear();
        taggedActions.clear();
    }
}

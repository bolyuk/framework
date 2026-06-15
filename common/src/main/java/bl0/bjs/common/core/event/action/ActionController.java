package bl0.bjs.common.core.event.action;

import bl0.bjs.common.core.tuple.Pair;

import java.util.ArrayList;
import java.util.HashMap;

public class ActionController<T> implements IActionController<T, Action<T>> {

    protected final ArrayList<Action<T>> actions = new  ArrayList<>();
    protected final ArrayList<Pair<Integer,Action<T>>> scheduledActions = new ArrayList<>();
    protected final HashMap<String, TaggedAction<T>> taggedActions = new HashMap<>();

    @Override
    public void schedule(int delay, Action<T> action) {
        scheduledActions.add(new Pair<>(delay, action));
    }

    @Override
    public void register(Action<T> action) {
        actions.add(action);
    }

    @Override
    public void unregister(Action<T> action) {
        actions.remove(action);
    }

    @Override
    public void registerUnique(TaggedAction<T> tagged) {
        taggedActions.put(tagged.tag(), tagged);
    }

    public void invoke(T data) {
        for(Action<T> action : new ArrayList<>(actions))
            action.invoke(data);

        for(TaggedAction<T> taggedAction : new ArrayList<>(taggedActions.values()))
            taggedAction.invoke(data);

        for (var pair : scheduledActions) {
            pair.first--;
            if(pair.first == 0)
                pair.second.invoke(data);
        }
    }

    public void clear(){
        actions.clear();
        taggedActions.clear();
    }
}

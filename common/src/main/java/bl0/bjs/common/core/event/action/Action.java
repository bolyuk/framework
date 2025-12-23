package bl0.bjs.common.core.event.action;

import bl0.bjs.common.core.event.base.Invokable;

@FunctionalInterface
public interface Action<T> extends Invokable {
     void invoke(T data);
}

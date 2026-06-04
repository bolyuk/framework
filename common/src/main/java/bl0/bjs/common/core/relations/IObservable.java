/*
 * Decompiled with CFR 0.152.
 */
package bl0.bjs.common.core.relations;

import bl0.bjs.common.core.event.action.Action;

public interface IObservable<R, E> {
    R addListener(Action<E> action);

    R remListener(Action<E> action);

    R invokeChangeAction();

    E get();

    R set(E value);
}


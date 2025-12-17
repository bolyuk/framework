/*
 * Decompiled with CFR 0.152.
 */
package bl0.bjs.common.core.relations;

import bl0.bjs.common.core.event.Action;

public interface INotifier<R, E> {
    public R addListener(Action<E> action);

    public R remListener(Action<E> action);

    public R invoke();
}


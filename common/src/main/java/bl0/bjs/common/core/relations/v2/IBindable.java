package bl0.bjs.common.core.relations.v2;

import bl0.bjs.common.core.relations.IObservable;

public interface IBindable<R, E> extends IObservable<R, E> {
    R bind(IObservable<R, E> observable);
    R unbind(IObservable<R, E> observable);

    R bindBidirectional(IBindable<R, E> observable);
    R unbindBidirectional(IBindable<R, E> observable);
}

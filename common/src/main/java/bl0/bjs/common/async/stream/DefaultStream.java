package bl0.bjs.common.async.stream;

import bl0.bjs.common.async.control.IAsync;
import bl0.bjs.common.core.event.action.Action;
import bl0.bjs.common.core.tuple.Pair;
import lombok.Setter;

import java.util.function.Function;

public class DefaultStream<T> implements IStream<T> {
    private T data;

    public final Action<DefaultStream<T>> work;

    @Setter
    public IAsync onCancel;

    @Setter
    private Function<Pair<StreamChunk<T>, T>, T> accumulator;

    @Setter
    private Action<T> deltaListener;

    public DefaultStream(Action<DefaultStream<T>> work) {
        this.work = work;
    }

    @Override
    public void feed(StreamChunk<T> data) {
        if(accumulator != null)
            this.data = accumulator.apply(Pair.of(data, this.data));

        if(deltaListener != null)
            deltaListener.invoke(data.data);
    }

    public void feedGeneric(StreamChunk<?> data) {
        feed((StreamChunk<T>) data);
    }

    @Override
    public void cancel() {
        if(onCancel != null)
            onCancel.run();
    }

    @Override
    public void start() {
        if(work != null)
            work.invoke(this);
    }
}

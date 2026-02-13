package bl0.bjs.common.async.stream;

import bl0.bjs.common.core.event.action.Action;
import bl0.bjs.common.core.tuple.Pair;

import java.util.function.Function;

public interface IStream<T> {
    void feed(StreamChunk<T> data);

    void cancel();

    void start();

    void setAccumulator(Function<Pair<StreamChunk<T>, T>, T> accumulator);

    void setDeltaListener(Action<T> deltaListener);
}

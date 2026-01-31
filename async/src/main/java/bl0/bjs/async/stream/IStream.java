package bl0.bjs.async.stream;

import bl0.bjs.common.core.tuple.Pair;

import java.util.function.Function;

public interface IStream<T> {
    void feed(StreamChunk<T> data);

    void cancel();

    void start();

    void setAccumulator(Function<Pair<StreamChunk<T>, T>, T> accumulator);
}

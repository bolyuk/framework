package bl0.bjs.socket.services.proxy.stream;

import bl0.bjs.common.async.control.IAsync;
import bl0.bjs.common.async.stream.IStream;
import bl0.bjs.common.async.stream.StreamChunk;
import bl0.bjs.common.core.event.action.Action;
import bl0.bjs.common.core.tuple.Pair;
import lombok.Setter;

import java.util.function.Function;

public class RemoteStreamController<T> implements IStream<T> {

    private IStreamCallbackPipe callback;
    @Setter
    public IAsync onCancel;

    public final Action<RemoteStreamController<T>> work;

    public RemoteStreamController(Action<RemoteStreamController<T>> work) {
        this.work = work;
    }

    public void bindWS(IStreamCallbackPipe callback) {
        this.callback = callback;
    }

    @Override
    public void feed(StreamChunk<T> data) {
        if(callback == null) throw new NullPointerException("IStreamCallbackPipe is null");
        callback.feed(data);
    }

    @Override
    public void cancel() {
        if(onCancel != null)
            onCancel.run();
    }

    @Override
    public void start() {
        work.invoke(this);
    }

    @Override
    public void setAccumulator(Function<Pair<StreamChunk<T>, T>, T> accumulator) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void setDeltaListener(Action<T> deltaListener) {
        throw new UnsupportedOperationException("Not supported.");
    }
}

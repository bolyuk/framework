package bl0.bjs.common.async.stream;

public interface IStreamCallbackPipe {
    void feed(StreamChunk<?> data);
}

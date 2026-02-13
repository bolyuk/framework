package bl0.bjs.common.async.stream;

public class StreamChunk<T> {
    public final boolean isFaulted;
    public final String errorMessage;
    public final boolean isDone;
    public final T data;

    public StreamChunk(boolean isDone, T data) {
        this.isDone = isDone;
        this.data = data;
        this.errorMessage = null;
        this.isFaulted = false;
    }

    public StreamChunk(String errorMessage) {
        this.isDone = false;
        this.data = null;
        this.errorMessage = errorMessage;
        this.isFaulted = true;
    }
}

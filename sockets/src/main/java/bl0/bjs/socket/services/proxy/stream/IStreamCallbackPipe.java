package bl0.bjs.socket.services.proxy.stream;

import bl0.bjs.common.async.stream.StreamChunk;

public interface IStreamCallbackPipe {
    void feed(StreamChunk<?> data);
}

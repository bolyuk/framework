package bl0.bjs.common.async.stream;

import bl0.bjs.common.async.stream.chunk.RangeChunk;
import bl0.bjs.common.async.stream.chunk.StreamChunk;

public interface IStreamDataSource<DATA, CHUNK extends RangeChunk<DATA>, STREAM_ID> {
    IStream<CHUNK> generateStream(STREAM_ID streamId);
    void requestRange(STREAM_ID streamId, Long start, Long count);
    void detachStream(STREAM_ID streamId);
}

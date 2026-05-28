package bl0.bjs.common.async.stream;

import bl0.bjs.common.async.stream.chunk.RangeChunk;

public interface IStreamDataSource<DATA, STREAM_ID> {
    public IStream<RangeChunk<DATA>> generateStream(STREAM_ID streamId);
    public void requestRange(STREAM_ID streamId, Long start, Long count);
}

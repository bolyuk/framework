package bl0.bjs.common.async.stream;

import java.util.List;
import java.util.Optional;

public interface IStreamDataSource<DATA, STREAM_ID> {
    public IStream<RangeChunk<DATA>> generateStream(STREAM_ID streamId);
    public void requestRange(STREAM_ID streamId, Long start, Long count);

    class RangeChunk<DATA> {
        public long startIndex;
        public List<DataInfo<DATA>> rangeData;
    }

    record DataInfo<DATA>(DataState state, DATA data) {

            public static <T> DataInfo<T> loaded(T data) {
                return new DataInfo<>(DataState.LOADED, data);
            }

            public static <T> DataInfo<T> placeholder() {
                return new DataInfo<>(DataState.PLACEHOLDER, null);
            }

            public static <T> DataInfo<T> error() {
                return new DataInfo<>(DataState.ERROR, null);
            }
        }

    enum DataState {
        LOADED,
        PLACEHOLDER,
        ERROR
    }
}

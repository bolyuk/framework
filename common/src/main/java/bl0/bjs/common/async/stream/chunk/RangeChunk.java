package bl0.bjs.common.async.stream.chunk;

import bl0.bjs.common.async.stream.data.DataInfo;

import java.util.List;

public class RangeChunk<DATA> {
    public long startIndex;
    public List<DataInfo<DATA>> rangeData;
}

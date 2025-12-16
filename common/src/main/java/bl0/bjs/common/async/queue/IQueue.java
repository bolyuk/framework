package bl0.bjs.common.async.queue;

import java.util.List;

public interface IQueue<T> {
    void pass(List<T> values);
}

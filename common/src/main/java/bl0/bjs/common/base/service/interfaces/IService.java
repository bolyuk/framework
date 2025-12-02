package bl0.bjs.common.base.service.interfaces;

public interface IService extends AutoCloseable {

    @Override
    default void close() {};
}

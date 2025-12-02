package bl0.bjs.services.interfaces;

public interface IService extends AutoCloseable {

    @Override
    default void close() {};
}

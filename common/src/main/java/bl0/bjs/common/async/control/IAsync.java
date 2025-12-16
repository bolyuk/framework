package bl0.bjs.common.async.control;

public interface IAsync extends Runnable{
    default void beforeInterrupt(){}
}

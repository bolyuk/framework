package bl0.bjs.common.core.async;

public interface IAsync extends Runnable{

    default void start(){
         AsyncExecutor.register(this);
    }

    default void stop(){
         AsyncExecutor.unregister(this);
    }
}

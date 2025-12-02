package bl0.bjs.common.core.time;

public class Timer {
    private long startTime;
    private long endTime;

    public Timer(){}

    public void start(){
        this.startTime = System.currentTimeMillis();
        this.endTime = this.startTime;
    }

    public long stop(){
        this.endTime = System.currentTimeMillis();
        return get();
    }

    public long get(){
        return this.endTime - this.startTime;
    }


}

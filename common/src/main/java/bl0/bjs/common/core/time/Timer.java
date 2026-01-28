package bl0.bjs.common.core.time;

public class Timer {
    private long startTime;
    private long endTime;

    private boolean isDone;

    public Timer(){}

    public void start(){
        this.startTime = System.currentTimeMillis();
        this.endTime = this.startTime;
    }

    public long stop(){
        this.endTime = System.currentTimeMillis();
        isDone = true;
        return get();
    }

    public boolean isDone(){
        return isDone;
    }

    public long get(){
        return this.endTime - this.startTime;
    }


}

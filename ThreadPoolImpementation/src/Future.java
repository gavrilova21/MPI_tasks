public class Future {
    private Boolean done=false;
    private Runnable task;

    public Future(Runnable runnable) {
        this.task = runnable;
        this.done = false;       // ensure visibility of callable
    }
    void done(){
       this.done = true;
    }
    Runnable getTask(){
        return task;
    }
    public synchronized Boolean isDone() {
        return done;
    }

}

import java.util.Queue;
import java.util.LinkedList;
public class BlockingQueue {

    private Queue queue = new LinkedList();

    public synchronized Object poll() {
        Object msg = null;
        while (queue.isEmpty()) {
            try {
                wait();
            } catch (InterruptedException e) {
                // Error return the client a null item
                return msg;
            }
        }
        msg = queue.remove();
        return msg;
    }

    public synchronized void add(Object o) {
        queue.add(o);
        // Wake up anyone waiting for something to be put on the queue.
        notifyAll();
    }
    public synchronized boolean isEmpty(){
        return queue.isEmpty();
    }
}
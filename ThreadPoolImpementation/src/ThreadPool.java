public class ThreadPool {
    private final int nThreads;
    private final BlockingQueue queue;

    public ThreadPool(int nThreads) {
        this.nThreads = nThreads;
        queue = new BlockingQueue();
        PoolWorker[] threads = new PoolWorker[nThreads];

        for (int i = 0; i < nThreads; i++) {
            threads[i] = new PoolWorker();
            threads[i].start();
        }
    }

    public Future execute(Runnable task) {
        Future future = new Future(task);
        synchronized (queue) {
            queue.add(future);
        }
        return future;
    }

    private class PoolWorker extends Thread {
        public void run() {
            Future future;

            while (true) {
                synchronized (queue) {
                    while (queue.isEmpty()) {
                        try {
                            queue.wait();
                        } catch (InterruptedException e) {
                            System.out.println("An error occurred while queue is waiting: " + e.getMessage());
                        }
                    }
                    future = (Future) queue.poll();
                }

                // If we don't catch RuntimeException,
                // the pool could leak threads
                try {
                    future.getTask().run();
                    future.done();
                } catch (RuntimeException e) {
                    System.out.println("Thread pool is interrupted due to an issue: " + e.getMessage());
                }
            }
        }
    }
}

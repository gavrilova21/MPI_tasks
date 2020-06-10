public class Main {

    public static void main(String[] args) throws InterruptedException {
        ThreadPool pool = new ThreadPool(7);

        Future future =  pool.execute(() -> {
            System.out.println("Hello");
        });
        while (!future.isDone()) {
            Thread.sleep(10);
        }
    }
}

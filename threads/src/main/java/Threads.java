import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Robert Tamas
 */
public class Threads {
    private static AtomicInteger number = new AtomicInteger(0);

    public static void main(String[] args) {
        int totalThreads = 3;
        ExecutorService executorService =
                Executors.newFixedThreadPool(totalThreads);
        for (int i = 1; i <= totalThreads; i++) {
            executorService.execute(new Threads.CustomThread("thread " + i));
        }
        executorService.shutdown();
    }

    static class CustomThread implements Runnable {
        private static AtomicInteger numberToPrint = new AtomicInteger(0);
        private String name;

        public CustomThread(String name) {
            this.name = name;
        }

        public void run() {
            while (true) {
                int stopNum = number.getAndIncrement();
                if (stopNum > 50){
                    break;
                }
                printLine(stopNum, name);
                for (long l = 0; l < 9999999; l++) {
                }
            }
        }

        private void printLine(int number, String thread) {
            while (numberToPrint.get() != number) {
            }
            System.out.println(thread + ": " + number);
            for (long l = 0; l < 9999999; l++) {
            }
            numberToPrint.getAndIncrement();
        }
    }
}

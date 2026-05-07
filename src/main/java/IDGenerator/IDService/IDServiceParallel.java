package IDGenerator.IDService;

import IDGenerator.PrimeNumberGenerator.PrimeNumberGenerator;

import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class IDServiceParallel implements IDServiceInterface {

    private static final int QUEUE_CAPACITY = 4096;

    private final PrimeNumberGenerator primeNumberGenerator;
    private final Set<Long> generatedIds = ConcurrentHashMap.newKeySet();
    private final BlockingQueue<Long> readyIds = new LinkedBlockingQueue<>(QUEUE_CAPACITY);
    private final ExecutorService producers;

    public IDServiceParallel(long lowerLimit, long upperLimit) {
        if (upperLimit < lowerLimit) {
            throw IDServiceException.lowerLimitHigherThanUpperLimit();
        }
        this.primeNumberGenerator = new PrimeNumberGenerator(lowerLimit, upperLimit);

        int producerCount = Math.max(4, Runtime.getRuntime().availableProcessors());
        this.producers = Executors.newFixedThreadPool(producerCount, runnable -> {
            Thread thread = new Thread(runnable, "IDServiceParallel-Producer");
            thread.setDaemon(true);
            return thread;
        });
        for (int i = 0; i < producerCount; i++) {
            producers.submit(this::producerLoop);
        }
    }

    private void producerLoop() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                long candidate = primeNumberGenerator.getRandomPrimeNumberInRange();
                if (candidate > 1 && generatedIds.add(candidate)) {
                    readyIds.put(candidate);
                }
            }
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public long getUnusedId() {
        try {
            return readyIds.take();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IDServiceException("Interrupted while waiting for an unused ID");
        }
    }

    public void shutdown() {
        producers.shutdownNow();
    }
}

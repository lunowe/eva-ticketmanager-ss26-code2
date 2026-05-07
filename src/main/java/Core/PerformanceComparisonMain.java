package Core;

import Core.Clients.PerformanceClient;
import IDGenerator.IDService.IDService;
import IDGenerator.IDService.IDServiceParallel;

public class PerformanceComparisonMain {

    private static final long LOWER_LIMIT = 100000000000000000L;
    private static final long UPPER_LIMIT = 999999999999999999L;

    public static void main(String[] args) {
        System.out.println("===== IDService (sequential) =====");
        long sequentialStart = System.currentTimeMillis();
        new PerformanceClient(new IDService(LOWER_LIMIT, UPPER_LIMIT)).run();
        long sequentialTotal = System.currentTimeMillis() - sequentialStart;

        System.out.println();
        System.out.println("===== IDServiceParallel (parallel) =====");
        IDServiceParallel parallelService = new IDServiceParallel(LOWER_LIMIT, UPPER_LIMIT);
        long parallelStart = System.currentTimeMillis();
        new PerformanceClient(parallelService).run();
        long parallelTotal = System.currentTimeMillis() - parallelStart;
        parallelService.shutdown();

        System.out.println();
        System.out.println("===== Summary =====");
        System.out.println("Sequential total: " + sequentialTotal + "ms");
        System.out.println("Parallel total:   " + parallelTotal + "ms");
        if (parallelTotal > 0) {
            double speedup = (double) sequentialTotal / parallelTotal;
            System.out.printf("Speedup:          %.2fx%n", speedup);
        }
    }
}

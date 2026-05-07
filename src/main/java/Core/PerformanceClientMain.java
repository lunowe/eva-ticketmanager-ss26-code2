package Core;

import Core.Clients.PerformanceClient;
import IDGenerator.IDService.IDService;

public class PerformanceClientMain {
    public static void main(String[] args) {
        IDService idService = new IDService(100000000000000000L, 999999999999999999L);

        PerformanceClient performanceClient = new PerformanceClient(idService);
        performanceClient.run();
    }
}

package by.aurorasoft.easy.recovery;

import java.util.Collection;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SchedulerService {

    private final ScheduledExecutorService scheduler;
    private final BackupService backupService;

    public SchedulerService(BackupService backupService, int schedulerPoolSize) {
        this.scheduler = Executors.newScheduledThreadPool(schedulerPoolSize);
        this.backupService = backupService;
    }

    public void start(Collection<EasyRecoverable<?>> services) {
        services.stream()
                .filter(s -> !s.backupPeriod().isZero())
                .forEach(s -> scheduler.scheduleAtFixedRate(
                        () -> backupService.save(s),
                        s.backupPeriod().toSeconds(),
                        s.backupPeriod().toSeconds(),
                        TimeUnit.SECONDS
                ));
    }

    public void shutdown() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
                System.out.println("### [EasyRecovery]: Forced shutdown of scheduler.");
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
            System.out.println("### [EasyRecovery]: Scheduler shutdown interrupted." + e.getMessage());
        }
    }
}

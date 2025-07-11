package by.aurorasoft.easy.recovery;

import by.aurorasoft.easy.recovery.exceptions.EasyRecoveryException;
import by.aurorasoft.easy.recovery.exceptions.EasyRecoveryScheduleBackupException;

import java.util.Collection;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SchedulerService {

    private final ScheduledExecutorService scheduler;
    private final BackupService backupService;
    private final EasyRecoveryExceptionHandler exceptionHandler;

    public SchedulerService(BackupService backupService, int schedulerPoolSize, EasyRecoveryExceptionHandler exceptionHandler) {
        this.scheduler = Executors.newScheduledThreadPool(schedulerPoolSize);
        this.backupService = backupService;
        this.exceptionHandler = exceptionHandler;
        System.out.printf("[EasyRecovery][INFO]: Scheduler initialized with pool size: %d%n", schedulerPoolSize);
    }

    public void start(Collection<EasyRecoverable<?>> services) {
        services.stream()
                .filter(s -> !s.backupPeriod().isZero())
                .forEach(s -> {
                    scheduler.scheduleAtFixedRate(
                            () -> {
                                try {
                                    backupService.save(s);
                                    System.out.printf("[EasyRecovery][INFO]: Periodic backup completed for service: %s%n",
                                            s.getClass().getSimpleName());
                                } catch (Exception e) {
                                    System.err.printf("[EasyRecovery][ERROR]: Failed to execute periodic backup for service: %s. Error: %s%n",
                                            s.getClass().getSimpleName(), e.getMessage());
                                    e.printStackTrace();
                                    exceptionHandler.handle(new EasyRecoveryScheduleBackupException(String.format("Failed to execute periodic backup for service: %s", s.getClass().getSimpleName()), e));
                                }
                            },
                            s.backupPeriod().toSeconds(),
                            s.backupPeriod().toSeconds(),
                            TimeUnit.SECONDS
                    );
                    System.out.printf("[EasyRecovery][INFO]: Scheduled periodic backup for service: %s with interval: %d seconds%n",
                            s.getClass().getSimpleName(), s.backupPeriod().toSeconds());
                });
    }

    public void shutdown() {
        System.out.println("[EasyRecovery][INFO]: Shutting down scheduler.");
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
                System.out.println("[EasyRecovery][WARN]: Forced shutdown of scheduler due to timeout.");
            } else {
                System.out.println("[EasyRecovery][INFO]: Scheduler shut down gracefully.");
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
            System.err.printf("[EasyRecovery][ERROR]: Scheduler shutdown interrupted. Error: %s%n", e.getMessage());
            exceptionHandler.handle(new EasyRecoveryException("Failed to execute gracefully shutdown", e));
        }
    }
}

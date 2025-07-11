package by.aurorasoft.easy.recovery;

import by.aurorasoft.easy.recovery.exceptions.EasyRecoveryBackupException;
import by.aurorasoft.easy.recovery.exceptions.EasyRecoveryException;
import by.aurorasoft.easy.recovery.exceptions.EasyRecoveryRestoreException;

import java.util.List;
import java.util.Objects;

/**
 * The {@code EasyRecoveryService} class orchestrates the backup and restoration process
 * for a collection of services implementing the {@code EasyRecoverable} interface.
 *
 * <p>It integrates with the {@code RestoreService}, {@code BackupService}, and
 * {@code SchedulerService} to ensure seamless management of service states, including
 * restoration at startup and periodic backups during the application's runtime.</p>
 */
public class EasyRecoveryService {

    private final List<EasyRecoverable<?>> services;
    private final RestoreService restoreService;
    private final BackupService backupService;
    private final SchedulerService schedulerService;
    private final EasyRecoveryExceptionHandler exceptionHandler;

    /**
     * Constructs an {@code EasyRecoveryService} instance with the specified components.
     *
     * @param services         the list of recoverable services to be managed
     * @param restoreService   the service responsible for restoring service states
     * @param backupService    the service responsible for saving service states
     * @param schedulerService the service responsible for scheduling periodic backups
     */
    public EasyRecoveryService(List<EasyRecoverable<?>> services,
                               RestoreService restoreService,
                               BackupService backupService,
                               SchedulerService schedulerService,
                               EasyRecoveryExceptionHandler exceptionHandler) {
        this.services = Objects.requireNonNullElse(services, List.of());
        this.restoreService = restoreService;
        this.backupService = backupService;
        this.schedulerService = schedulerService;
        this.exceptionHandler = exceptionHandler;
    }

    /**
     * Starts the recovery service by restoring states for all managed services
     * and scheduling periodic backups for them.
     *
     * <p>If an error occurs during the restoration process, it is logged,
     * and the system continues with the remaining services.</p>
     */
    public void start() {

        services.forEach(recoverable -> {
            try {
                restoreService.restore(recoverable);
            } catch (EasyRecoveryException e) {
                e.printStackTrace();
                System.err.println("[EasyRecovery]: Start restore fail for service: " + recoverable.getClass().getSimpleName() + " message: " + e.getMessage());
                exceptionHandler.handle(new EasyRecoveryRestoreException("Start restore fail for service: " + recoverable.getClass().getSimpleName(), e));
            }
        });

        schedulerService.start(services);
    }

    /**
     * Stops the recovery service by performing a final backup for all managed services
     * and shutting down the scheduler.
     *
     * <p>If an error occurs during the backup process, it is logged.</p>
     */
    public void stop() {
        System.out.println("[EasyRecovery]: Start stop Gracefully");
        schedulerService.shutdown();
        services.forEach(recoverable -> {
                    try {
                        backupService.save(recoverable);
                    } catch (Exception e) {
                        System.err.println("[EasyRecovery]: Backup is failed for service: " + recoverable.getClass().getSimpleName());
                        e.printStackTrace();
                        exceptionHandler.handle(new EasyRecoveryBackupException("Backup is failed for service: " + recoverable.getClass().getSimpleName(), e));
                    }
                }
        );
    }
}

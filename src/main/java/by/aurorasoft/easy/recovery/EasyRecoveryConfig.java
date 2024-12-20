package by.aurorasoft.easy.recovery;

import java.util.List;

/**
 * The {@code EasyRecoveryConfig} class serves as a base configuration for initializing and managing
 * the {@code EasyRecoveryService}.
 *
 * <p>Subclasses of this abstract class can customize the initialization of services
 * and provide specific configurations for backup, restoration, and scheduling.
 */
public abstract class EasyRecoveryConfig {

    private final int schedulerThreadPoolSize;
    private final List<EasyRecoverable<?>> services;

    /**
     * Constructs an {@code EasyRecoveryConfig} with the specified services and scheduler configuration.
     *
     * @param services              the list of services to be managed
     * @param schedulerThreadPoolSize the size of the thread pool for scheduling backups
     */
    public EasyRecoveryConfig(List<EasyRecoverable<?>> services,
                              int schedulerThreadPoolSize) {
        this.schedulerThreadPoolSize = schedulerThreadPoolSize;
        this.services = services;
    }

    /**
     * Creates and starts the {@code EasyRecoveryService} for managing the backup and restoration of services.
     *
     * <p>This method initializes the {@code EasyRecoveryService} with the provided services, a backup service,
     * a restore service, and a scheduler. It also registers a shutdown hook to ensure proper cleanup
     * during application termination.</p>
     *
     * @return an instance of {@code EasyRecoveryService} that is ready to manage services
     */
    public EasyRecoveryService easyRecoveryService() {
        BackupService backupService = new BackupService();
        EasyRecoveryService easyRecoveryService = new EasyRecoveryService(
                services,
                new RestoreService(),
                backupService,
                new SchedulerService(backupService, schedulerThreadPoolSize)
        );

        // Запуск
        easyRecoveryService.start();

        // Обработка завершения
        Runtime.getRuntime().addShutdownHook(new Thread(easyRecoveryService::stop));
        return easyRecoveryService;
    }
}

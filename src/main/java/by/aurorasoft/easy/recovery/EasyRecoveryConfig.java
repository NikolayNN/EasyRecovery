package by.aurorasoft.easy.recovery;

import sun.misc.Signal;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

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

    private final AtomicBoolean stopped = new AtomicBoolean(false);

    /**
     * Constructs an {@code EasyRecoveryConfig} with the specified services and scheduler configuration.
     *
     * @param services                the list of services to be managed
     * @param schedulerThreadPoolSize the size of the thread pool for scheduling backups
     */
    public EasyRecoveryConfig(List<EasyRecoverable<?>> services,
                              int schedulerThreadPoolSize) {
        this.schedulerThreadPoolSize = schedulerThreadPoolSize;
        this.services = services;
    }

    protected EasyRecoveryExceptionHandler exceptionHandler() {
        return EasyRecoveryExceptionHandler.NOOP;
    }

    /**
     * Creates and starts the {@code EasyRecoveryService} for managing the backup and restoration of services.
     *
     * <p>This method initializes the {@code EasyRecoveryService} with the provided services, a backup service,
     * a restore service, and a scheduler. It also registers mechanisms to ensure proper cleanup during
     * application termination.</p>
     *
     * <p>The following cleanup mechanisms are registered:</p>
     * <ul>
     *     <li>{@code Signal.handle(new Signal("TERM"), signal -> safeStop.run())}:
     *         Handles the {@code SIGTERM} signal, which is typically sent when the application is stopped
     *         (e.g., via {@code docker stop} or similar commands). This ensures that the {@code EasyRecoveryService}
     *         is properly stopped when the application receives a termination signal.</li>
     *     <li>{@code Runtime.getRuntime().addShutdownHook(new Thread(safeStop))}:
     *         Registers a shutdown hook that executes during JVM termination (e.g., when the JVM exits normally
     *         or is terminated). This acts as a fallback mechanism to ensure that {@code EasyRecoveryService.stop()}
     *         is called even if the application terminates without receiving a {@code SIGTERM} signal.</li>
     * </ul>
     *
     * <p>Both mechanisms utilize a thread-safe {@code safeStop} method, which ensures that the
     * {@code EasyRecoveryService.stop()} method is called only once, even if both cleanup mechanisms are triggered.</p>
     *
     * @return an instance of {@code EasyRecoveryService} that is ready to manage services
     */
    public EasyRecoveryService easyRecoveryService() {
        EasyRecoveryExceptionHandler exceptionHandler = exceptionHandler();
        BackupService backupService = new BackupService();
        EasyRecoveryService easyRecoveryService = new EasyRecoveryService(
                services,
                new RestoreService(),
                backupService,
                schedulerService(backupService, schedulerThreadPoolSize, exceptionHandler),
                exceptionHandler
        );

        // Запуск
        easyRecoveryService.start();

        Runnable safeStop = () -> {
            if (stopped.compareAndSet(false, true)) {
                easyRecoveryService.stop();
            }
        };

        // Обработка SIGTERM
        Signal.handle(new Signal("TERM"), signal -> safeStop.run());

        // Обработка Shutdown Hook
        Runtime.getRuntime().addShutdownHook(new Thread(safeStop));
        return easyRecoveryService;
    }

    protected SchedulerService schedulerService(BackupService backupService,
                                                int schedulerThreadPoolSize,
                                                EasyRecoveryExceptionHandler exceptionHandler) {
        return new SchedulerService(backupService, schedulerThreadPoolSize, exceptionHandler);
    }
}

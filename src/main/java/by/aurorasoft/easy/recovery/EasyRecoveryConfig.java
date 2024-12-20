package by.aurorasoft.easy.recovery;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

public abstract class EasyRecoveryConfig {
    private final ObjectMapper objectMapper;
    private final int schedulerThreadPoolSize;
    private final List<EasyRecoverable<?>> services;

    public EasyRecoveryConfig(List<EasyRecoverable<?>> services,
                              ObjectMapper objectMapper,
                              int schedulerThreadPoolSize) {
        this.objectMapper = objectMapper;
        this.schedulerThreadPoolSize = schedulerThreadPoolSize;
        this.services = services;
    }

    public EasyRecoveryService easyRecoveryService() {
        BackupService backupService = new BackupService(objectMapper);
        EasyRecoveryService easyRecoveryService = new EasyRecoveryService(
                services,
                new RestoreService(objectMapper),
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

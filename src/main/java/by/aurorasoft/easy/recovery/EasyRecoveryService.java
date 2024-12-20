package by.aurorasoft.easy.recovery;

import java.util.List;
import java.util.Objects;

public class EasyRecoveryService {

    private final List<EasyRecoverable<?>> services;
    private final RestoreService restoreService;
    private final BackupService backupService;
    private final SchedulerService schedulerService;

    public EasyRecoveryService(List<EasyRecoverable<?>> services,
                               RestoreService restoreService,
                               BackupService backupService,
                               SchedulerService schedulerService) {
        this.services = Objects.requireNonNullElse(services, List.of());
        this.restoreService = restoreService;
        this.backupService = backupService;
        this.schedulerService = schedulerService;
    }

    public void start() {
        try {
            services.forEach(restoreService::restore);
            schedulerService.start(services);
        } catch (EasyRecoveryException e) {
            System.err.println("Start restore fail: " + e.getMessage());
        }
    }

    public void stop() {
        try{
            schedulerService.shutdown();
            services.forEach(backupService::save);
        } catch (EasyRecoveryException e) {
            System.err.println("Backup fail: " + e.getMessage());
        }
    }
}

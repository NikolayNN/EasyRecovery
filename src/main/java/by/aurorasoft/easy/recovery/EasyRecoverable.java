package by.aurorasoft.easy.recovery;

import java.nio.file.Path;
import java.time.Duration;

public interface EasyRecoverable<S> {
    String backupPath();

    /**
     * Получить состояние для сохранения
     */
    S backup();

    /**
     * Восстановить состояние
     */
    void restore(S state);

    /**
     * Сохранять промежуточные версии с периодичностью
     */
    default Duration backupPeriod() {
        return Duration.ZERO;
    }
}

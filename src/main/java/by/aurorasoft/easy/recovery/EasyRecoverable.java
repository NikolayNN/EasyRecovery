package by.aurorasoft.easy.recovery;

import java.io.Serializable;
import java.time.Duration;

/**
 * The {@code EasyRecoverable} interface represents a contract for services that support
 * state backup and restoration. Classes implementing this interface must provide mechanisms
 * to save their state to a file and restore it from a file.
 *
 * <p>This interface is designed to integrate with the EasyRecovery library,
 * which manages the lifecycle of backups and periodic saving of service states.
 * The generic type {@code S} represents the type of the state object.</p>
 *
 * @param <S> the type of the state object to be backed up and restored
 */
public interface EasyRecoverable<S> {

    /**
     * Returns the file path where the state should be saved or restored from.
     *
     * @return the file path as a {@code String}
     */
    String backupPath();

    /**
     * Captures the current state of the service to be saved.
     * This method is called by the {@code BackupService} during the save operation.
     *
     * @return the current state of the service
     */
    S backup();

    /**
     * Restores the state of the service from the given state object.
     * This method is called by the {@code RestoreService} during the restore operation.
     *
     * @param state the state object to restore
     */
    void restore(S state);

    /**
     * Specifies the periodic interval for automatic backups.
     *
     * <p>By default, this method returns {@code Duration.ZERO}, indicating that
     * no periodic backups are required. Implementations can override this
     * method to specify a custom interval.</p>
     *
     * @return the backup period as a {@code Duration}
     */
    default Duration backupPeriod() {
        return Duration.ZERO;
    }
}

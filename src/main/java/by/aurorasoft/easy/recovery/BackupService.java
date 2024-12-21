package by.aurorasoft.easy.recovery;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class BackupService {

    BackupService() {
    }

    public void save(EasyRecoverable<?> recoverable) {
        String pathStr = recoverable.backupPath();
        File file = new File(pathStr);
        File parentDir = file.getParentFile();

        if (parentDir != null && !parentDir.exists() && !parentDir.mkdirs()) {
            throw new EasyRecoveryException("Failed to create directory for file: " + pathStr);
        }

        try {
            System.out.printf("[EasyRecovery][INFO]: Starting backup for service: %s%n", recoverable.getClass().getSimpleName());

            Object state = recoverable.backup();

            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
                oos.writeObject(state);
                System.out.printf("[EasyRecovery][INFO]: Successfully saved state for service: %s to file: %s%n",
                        recoverable.getClass().getSimpleName(), pathStr);
            }

        } catch (IOException e) {
            e.printStackTrace();
            throw new EasyRecoveryException("Failed to save state to file: " + pathStr, e);
        }
    }
}

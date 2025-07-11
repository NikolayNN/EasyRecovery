package by.aurorasoft.easy.recovery;

import by.aurorasoft.easy.recovery.exceptions.EasyRecoveryException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

public class RestoreService {

    public void restore(EasyRecoverable<?> recoverable) {
        String pathStr = recoverable.backupPath();
        File file = new File(pathStr);

        if (!file.exists()) {
            System.out.printf("[EasyRecovery][WARN]: State for service '%s' not restored. File not found: %s%n",
                    recoverable.getClass().getSimpleName(), pathStr);
            return;
        }

        try {
            System.out.printf("[EasyRecovery][INFO]: Starting restore process for service: %s%n",
                    recoverable.getClass().getSimpleName());

            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                Object state = ois.readObject();

                @SuppressWarnings("unchecked")
                EasyRecoverable<Object> castedRecoverable = (EasyRecoverable<Object>) recoverable;
                castedRecoverable.restore(state);

                System.out.printf("[EasyRecovery][INFO]: Successfully restored state for service: %s%n",
                        recoverable.getClass().getSimpleName());
            }

        } catch (IOException | ClassNotFoundException e) {
            throw new EasyRecoveryException("Failed to restore state from file: " + pathStr, e);
        }
    }
}

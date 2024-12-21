package by.aurorasoft.easy.recovery;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class RestoreService {

    private final ObjectMapper objectMapper;

    RestoreService(ObjectMapper mapper) {
        this.objectMapper = mapper;
    }

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

            String fileContent = Files.readString(file.toPath());
            Object state = RestoreUtils.restore(fileContent, recoverable, objectMapper);
            @SuppressWarnings("unchecked")
            EasyRecoverable<Object> castedRecoverable = (EasyRecoverable<Object>) recoverable;
            castedRecoverable.restore(state);
            System.out.printf("[EasyRecovery][INFO]: Successfully restored state for service: %s%n",
                    recoverable.getClass().getSimpleName());
        } catch (IOException e) {
            throw new EasyRecoveryException("Failed to restore state from file: " + pathStr, e);
        }
    }
}

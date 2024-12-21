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
            System.err.println("### [EasyRecovery]: File was not found " + pathStr);
            return;
        }

        try {
            String fileContent = Files.readString(file.toPath());
            Object state = RestoreUtils.restore(fileContent, recoverable, objectMapper);
            @SuppressWarnings("unchecked")
            EasyRecoverable<Object> castedRecoverable = (EasyRecoverable<Object>) recoverable;
            castedRecoverable.restore(state);
        } catch (IOException e) {
            throw new EasyRecoveryException("Failed to restore state from file: " + pathStr, e);
        }
    }
}

package by.aurorasoft.easy.recovery;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;

public class BackupService {

    private final ObjectMapper objectMapper;

    BackupService(ObjectMapper mapper) {
        this.objectMapper = mapper;
    }

    public void save(EasyRecoverable<?> recoverable) {
        String pathStr = recoverable.backupPath();
        File file = new File(pathStr);
        File parentDir = file.getParentFile();

        if (parentDir != null && !parentDir.exists() && !parentDir.mkdirs()) {
            throw new EasyRecoveryException("Failed to create directory for file: " + pathStr);
        }

        try {
            Object state = recoverable.backup();
            objectMapper.writeValue(file, state);
        } catch (IOException e) {
            throw new EasyRecoveryException("Failed to save state to file: " + pathStr, e);
        }
    }
}

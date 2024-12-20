package by.aurorasoft.easy.recovery;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class BackupServiceTest {

    private BackupService backupService;
    private ObjectMapper objectMapper;

    static class TestRecoverable implements EasyRecoverable<Map<String, Object>> {
        private Map<String, Object> state;
        private final String backupPath;

        public TestRecoverable(String backupPath, Map<String, Object> state) {
            this.backupPath = backupPath;
            this.state = state;
        }

        @Override
        public String backupPath() {
            return backupPath;
        }

        @Override
        public Map<String, Object> backup() {
            return state;
        }

        @Override
        public void restore(Map<String, Object> state) {
            this.state = state;
        }
    }

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        backupService = new BackupService(objectMapper);
    }

    @Test
    void testSaveWithValidData(@TempDir Path tempDir) throws IOException {
        Path file = tempDir.resolve("backup.json");
        Map<String, Object> state = Map.of("key", "value", "number", 123);
        TestRecoverable recoverable = new TestRecoverable(file.toString(), state);

        backupService.save(recoverable);

        String content = Files.readString(file);
        assertTrue(content.contains("\"key\":\"value\""));
        assertTrue(content.contains("\"number\":123"));
    }

    @Test
    void testSaveCreatesDirectories(@TempDir Path tempDir) throws IOException {
        Path subDir = tempDir.resolve("sub/dir");
        Path file = subDir.resolve("backup.json");
        Map<String, Object> state = Map.of("testKey", "testValue");
        TestRecoverable recoverable = new TestRecoverable(file.toString(), state);

        backupService.save(recoverable);

        assertTrue(Files.exists(file));
        String content = Files.readString(file);
        assertTrue(content.contains("\"testKey\":\"testValue\""));
    }
}

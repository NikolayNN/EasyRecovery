package by.aurorasoft.easy.recovery;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class BackupServiceTest {

    private BackupService backupService;

    static class TestRecoverable implements EasyRecoverable<Map<String, Object>>, Serializable {
        private static final long serialVersionUID = 1L;
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
        backupService = new BackupService();
    }

    @Test
    void testSaveWithValidData(@TempDir Path tempDir) throws IOException, ClassNotFoundException {
        Path file = tempDir.resolve("backup.ser");
        Map<String, Object> state = Map.of("key", "value", "number", 123);
        TestRecoverable recoverable = new TestRecoverable(file.toString(), state);

        backupService.save(recoverable);

        // Проверяем, что файл существует
        assertTrue(Files.exists(file), "Backup file should exist.");

        // Проверяем, что содержимое корректно десериализуется
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file.toFile()))) {
            @SuppressWarnings("unchecked")
            Map<String, Object> restoredState = (Map<String, Object>) ois.readObject();
            assertEquals("value", restoredState.get("key"), "Key should match expected value.");
            assertEquals(123, restoredState.get("number"), "Number should match expected value.");
        }
    }

    @Test
    void testSaveCreatesDirectories(@TempDir Path tempDir) throws IOException, ClassNotFoundException {
        Path subDir = tempDir.resolve("sub/dir");
        Path file = subDir.resolve("backup.ser");
        Map<String, Object> state = Map.of("testKey", "testValue");
        TestRecoverable recoverable = new TestRecoverable(file.toString(), state);

        backupService.save(recoverable);

        // Проверяем, что файл существует
        assertTrue(Files.exists(file), "Backup file should exist in created directories.");

        // Проверяем, что содержимое корректно десериализуется
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file.toFile()))) {
            @SuppressWarnings("unchecked")
            Map<String, Object> restoredState = (Map<String, Object>) ois.readObject();
            assertEquals("testValue", restoredState.get("testKey"), "Value should match expected state.");
        }
    }

}

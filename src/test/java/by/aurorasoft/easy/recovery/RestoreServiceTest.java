package by.aurorasoft.easy.recovery;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class RestoreServiceTest {

    private RestoreService restoreService;

    static class TestRecoverable implements EasyRecoverable<Map<String, Object>>, Serializable {
        private static final long serialVersionUID = 1L;
        private Map<String, Object> state;
        private final String backupPath;

        public TestRecoverable(String backupPath) {
            this.backupPath = backupPath;
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

        public Map<String, Object> getState() {
            return state;
        }
    }

    @BeforeEach
    void setUp() {
        restoreService = new RestoreService();
    }

    @Test
    void testRestoreWithValidData(@TempDir Path tempDir) throws IOException, ClassNotFoundException {
        Path file = tempDir.resolve("backup.ser");
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file.toFile()))) {
            oos.writeObject(Map.of("key", "value", "number", 123));
        }

        TestRecoverable recoverable = new TestRecoverable(file.toString());
        restoreService.restore(recoverable);

        assertNotNull(recoverable.getState(), "State should not be null after restore.");
        assertEquals("value", recoverable.getState().get("key"), "Key should match expected value.");
        assertEquals(123, recoverable.getState().get("number"), "Number should match expected value.");
    }

    @Test
    void testRestoreWithNonExistentFile() {
        TestRecoverable recoverable = new TestRecoverable("non_existent.ser");
        restoreService.restore(recoverable);
        assertNull(recoverable.getState(), "State should be null if file does not exist.");
    }

    @Test
    void testRestoreWithEmptyData(@TempDir Path tempDir) throws IOException {
        Path file = tempDir.resolve("empty.ser");
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file.toFile()))) {
            oos.writeObject(Map.of());
        }

        TestRecoverable recoverable = new TestRecoverable(file.toString());
        restoreService.restore(recoverable);

        assertNotNull(recoverable.getState(), "State should not be null after restore.");
        assertTrue(recoverable.getState().isEmpty(), "Restored state should be empty.");
    }

}

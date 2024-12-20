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

class RestoreServiceTest {

    private RestoreService restoreService;
    private ObjectMapper objectMapper;

    static class TestRecoverable implements EasyRecoverable<Map<String, Object>> {
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
        objectMapper = new ObjectMapper();
        restoreService = new RestoreService(objectMapper);
    }

    @Test
    void testRestoreWithValidJson(@TempDir Path tempDir) throws IOException {
        Path file = tempDir.resolve("backup.json");
        String jsonContent = "{\"key\":\"value\",\"number\":123}";
        Files.writeString(file, jsonContent);

        TestRecoverable recoverable = new TestRecoverable(file.toString());
        restoreService.restore(recoverable);

        assertNotNull(recoverable.getState());
        assertEquals("value", recoverable.getState().get("key"));
        assertEquals(123, recoverable.getState().get("number"));
    }

    @Test
    void testRestoreWithNonExistentFile() {
        TestRecoverable recoverable = new TestRecoverable("non_existent.json");
        restoreService.restore(recoverable);
        assertNull(recoverable.getState());
    }

    @Test
    void testRestoreWithInvalidJson(@TempDir Path tempDir) throws IOException {
        Path file = tempDir.resolve("invalid.json");
        String invalidJson = "{\"key\":\"value\",\"number\"}"; // Некорректный JSON
        Files.writeString(file, invalidJson);

        TestRecoverable recoverable = new TestRecoverable(file.toString());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            restoreService.restore(recoverable);
        });

    }

    @Test
    void testRestoreWithEmptyJson(@TempDir Path tempDir) throws IOException {
        Path file = tempDir.resolve("empty.json");
        Files.writeString(file, "{}");

        TestRecoverable recoverable = new TestRecoverable(file.toString());
        restoreService.restore(recoverable);

        assertNotNull(recoverable.getState());
        assertTrue(recoverable.getState().isEmpty());
    }

}

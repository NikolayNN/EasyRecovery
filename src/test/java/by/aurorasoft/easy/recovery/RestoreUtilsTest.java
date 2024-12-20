package by.aurorasoft.easy.recovery;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class RestoreUtilsTest {

    static class TestRecoverable implements EasyRecoverable<Map<String, Object>> {
        private Map<String, Object> state;

        @Override
        public String backupPath() {
            return "test.json";
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

    @Test
    void testRestoreWithValidJson() {
        ObjectMapper objectMapper = new ObjectMapper();
        TestRecoverable recoverable = new TestRecoverable();

        String validJson = "{\"key\":\"value\",\"number\":123}";

        Object restoredState = RestoreUtils.restore(validJson, recoverable, objectMapper);
        assertTrue(restoredState instanceof Map);
        assertEquals("value", ((Map<?, ?>) restoredState).get("key"));
        assertEquals(123, ((Map<?, ?>) restoredState).get("number"));
    }

    @Test
    void testRestoreWithWrongType() {
        ObjectMapper objectMapper = new ObjectMapper();
        TestRecoverable recoverable = new TestRecoverable();

        String jsonWithWrongType = "{\"key\":123,\"number\":\"value\"}";

        Object restoredState = RestoreUtils.restore(jsonWithWrongType, recoverable, objectMapper);
        assertTrue(restoredState instanceof Map);
        assertEquals(123, ((Map<?, ?>) restoredState).get("key"));
        assertEquals("value", ((Map<?, ?>) restoredState).get("number"));
    }

    @Test
    void testRestoreWithEmptyJson() {
        ObjectMapper objectMapper = new ObjectMapper();
        TestRecoverable recoverable = new TestRecoverable();

        String emptyJson = "{}";

        Object restoredState = RestoreUtils.restore(emptyJson, recoverable, objectMapper);
        assertTrue(restoredState instanceof Map);
        assertTrue(((Map<?, ?>) restoredState).isEmpty());
    }

}

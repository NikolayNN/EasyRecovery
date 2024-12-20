package by.aurorasoft.easy.recovery;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.lang.reflect.ParameterizedType;

class RestoreUtils {
    static Object restore(String json, EasyRecoverable<?> recoverable, ObjectMapper objectMapper) {

        java.lang.reflect.Type stateType = extractGenericType(recoverable);

        try {
            if (stateType instanceof Class<?>) {
                // Для обычных классов
                return objectMapper.readValue(json, (Class<?>) stateType);
            } else {
                // Для параметризованных типов (например, Map<String, Object>)
                return objectMapper.readValue(json, new TypeReference<>() {
                    @Override
                    public java.lang.reflect.Type getType() {
                        return stateType;
                    }
                });
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private static java.lang.reflect.Type extractGenericType(EasyRecoverable<?> recoverable) {
        for (java.lang.reflect.Type type : recoverable.getClass().getGenericInterfaces()) {
            if (type instanceof ParameterizedType paramType) {
                if (paramType.getRawType() == EasyRecoverable.class) {
                    return paramType.getActualTypeArguments()[0];
                }
            }
        }
        throw new RuntimeException("Не удалось извлечь тип состояния для " + recoverable.getClass());
    }
}

package by.aurorasoft.easy.recovery.exceptions;

public class EasyRecoveryException extends RuntimeException {

    public EasyRecoveryException(String message) {
        super(message);
    }

    public EasyRecoveryException(String message, Throwable cause) {
        super(message, cause);
    }
}

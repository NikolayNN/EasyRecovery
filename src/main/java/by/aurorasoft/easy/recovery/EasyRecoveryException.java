package by.aurorasoft.easy.recovery;

public class EasyRecoveryException extends RuntimeException {

    public EasyRecoveryException(String message) {
        super(message);
    }

    public EasyRecoveryException(String message, Throwable cause) {
        super(message, cause);
    }
}
